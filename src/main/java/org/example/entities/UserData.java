package org.example.entities;

import io.hypersistence.utils.hibernate.type.array.EnumArrayType;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.Parameter;
import lombok.*;
import jakarta.persistence.*;
import org.example.model.Gender;
import org.hibernate.annotations.Type;

import java.time.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "user_data")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ColumnTransformer(write = "CAST(? AS public.\"GENDER\")")
    private Gender gender;

    @Column(columnDefinition = "TEXT DEFAULT ''")
    @Builder.Default
    private String description = "";

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "active_video_id")
    private UserVideo activeVideo;

    @Column(name = "is_hidden", nullable = false)
    @Builder.Default
    private boolean hidden = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "preferred_gender", columnDefinition = "text")
    private String preferredGenderJson;

    @Transient
    private List<Gender> preferredGenders;

    @Column(name = "min_age")
    private Integer minAge;

    @Column(name = "max_age")
    private Integer maxAge;

    @Column(name = "radius_km")
    private Integer radiusKm;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public List<Gender> getPreferredGenders() {
        if (preferredGenderJson == null || preferredGenderJson.isBlank()) {
            return new ArrayList<>();
        }
        return Arrays.stream(preferredGenderJson.split(","))
                .map(Gender::valueOf)
                .collect(Collectors.toList());
    }

    public void setPreferredGenders(List<Gender> genders) {
        this.preferredGenders = genders;
        if (genders == null || genders.isEmpty()) {
            this.preferredGenderJson = null;
        } else {
            this.preferredGenderJson = genders.stream()
                    .map(Gender::name)
                    .collect(Collectors.joining(","));
        }
    }
}