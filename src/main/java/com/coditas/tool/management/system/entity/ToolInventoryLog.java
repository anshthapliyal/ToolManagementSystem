package com.coditas.tool.management.system.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tool_inventory_logs")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ToolInventoryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tool being assigned
    @ManyToOne
    @JoinColumn(name = "tool_id", nullable = false)
    private Tool tool;

    // ToolCrib receiving the tool
    @ManyToOne
    @JoinColumn(name = "tool_crib_id", nullable = false)
    private ToolCrib toolCrib;

    // Workplace to which tool is assigned
    @ManyToOne
    @JoinColumn(name = "workplace_id", nullable = false)
    private Workplace workplace;

    // Who assigned it (Facility Manager)
    @ManyToOne
    @JoinColumn(name = "assigned_by", nullable = false)
    private User assignedBy;

    @Column(name = "quantity_assigned", nullable = false)
    private Long quantityAssigned;

    @CreationTimestamp
    @Column(name = "assigned_at", nullable = false, updatable = false)
    private LocalDateTime assignedAt;
}
