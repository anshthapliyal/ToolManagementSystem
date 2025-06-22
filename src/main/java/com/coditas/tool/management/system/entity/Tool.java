package com.coditas.tool.management.system.entity;

import com.coditas.tool.management.system.constant.ToolCategory;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tools")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Tool {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @Column(name = "total_qty")
    private Long totalQty;

    @Column(name = "price")
    private Long price;

    @Column(name = "fine_amount")
    private Long fineAmount;

    @Column(name = "is_perishable")
    private Boolean isPerishable;

    @Column(name="return_period")
    private Integer returnPeriod;

    @Column(name = "tool_image_url")
    private String toolImageUrl;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "category")
    private ToolCategory category;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

}
