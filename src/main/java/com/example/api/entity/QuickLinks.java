package com.example.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "rbx_t_quick_links")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuickLinks {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "quick_links_seq")
    @SequenceGenerator(name = "quick_links_seq", sequenceName = "quick_links_sequence", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 255)
    @Column(name = "name_en", length = 255)
    private String nameEn;

    @Size(max = 255)
    @Column(name = "name_ar", length = 255)
    private String nameAr;

    @Pattern(regexp = "[YN]", message = "isActive must be 'Y' or 'N'")
    @Column(name = "is_active", length = 1)
    private String isActive;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = "Y"; // Default to active
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
