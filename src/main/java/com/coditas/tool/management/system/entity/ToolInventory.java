package com.coditas.tool.management.system.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "tool_inventory")  //Join Table of ToolCrib and Tool
public class ToolInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tool_crib_id", nullable = false)
    private ToolCrib toolCrib;

    @ManyToOne
    @JoinColumn(name = "tool_id", nullable = false)
    private Tool tool;

    @Column(name = "total_quantity", nullable = false)
    private Long totalQuantity;

    @Column(name = "available_quantity", nullable = false)
    private Long availableQuantity;

    @Column(name = "broken_quantity")
    private Long brokenQuantity = 0L;

    @Column(name = "minimum_threshold")
    private Long minimumThreshold; //to trigger restock warnings

    @UpdateTimestamp
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

}