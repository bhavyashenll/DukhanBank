package com.example.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "rbx_t_widget_master")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WidgetMaster
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "widget_id")
    private Long widgetId;

    @Pattern(regexp = "[YN]", message = "isActive must be 'Y' or 'N'")
    @Column(name = "status", length = 1, nullable = false)
    @Builder.Default
    private String status = "N";

    @Column(name = "widget_name_en", length = 100)
    private String widgetNameEn;  // English widget name

    @Column(name = "widget_name_ar", length = 100)
    private String widgetNameAr;  // Arabic widget name

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = "Y"; // Default to active
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
