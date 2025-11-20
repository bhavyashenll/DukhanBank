package com.example.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "rbx_t_segment_quicklink_map")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SegmentQuicklinkMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "segment_id", nullable = false)
    private Long segmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "segment_id", referencedColumnName = "id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_segment_quicklink_segment"))
    private Segments segment;

    @Column(name = "quicklink_id", nullable = false)
    private Long quicklinkId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quicklink_id", referencedColumnName = "id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_segment_quicklink_quicklink"))
    private QuickLinks quickLink;

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