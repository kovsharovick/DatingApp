package org.example.entities;

import io.hypersistence.utils.hibernate.type.array.ListArrayType;
import lombok.*;
import jakarta.persistence.*;
import org.example.model.Gender;
import org.hibernate.annotations.Type;

import java.time.*;
import java.util.List;

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
    private Gender gender;

    @Column(columnDefinition = "TEXT DEFAULT ''")
    private String description = "";

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "active_video_id")
    private UserVideo activeVideo;

    @Column(name = "is_hidden", nullable = false)
    private boolean hidden = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Type(ListArrayType.class)
    @Column(name = "preferred_gender", columnDefinition = "public.\"GENDER\"[]")
    private List<Gender> preferredGenders;

    @Column(name = "min_age")
    private Integer minAge;

    @Column(name = "max_age")
    private Integer maxAge;

    @Column(name = "radius_km")
    private Integer radiusKm;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}