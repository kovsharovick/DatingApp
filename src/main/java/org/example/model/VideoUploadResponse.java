package org.example.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoUploadResponse {
    private Long videoId;
    private String videoUrl;
}