package com.coditas.tool.management.system.entity;

import com.coditas.tool.management.system.constant.RequestStatus;
import com.coditas.tool.management.system.constant.ReturnStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "tool_request_items")
public class ToolRequestItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private ToolRequest toolRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tool_id", nullable = false)
    private Tool tool;

    @Column(name = "req_quantity", nullable = false)
    private Long reqQuantity;

    @Column(name = "ret_quantity")
    private Long retQuantity;

    @Column(name = "brk_quantity")
    private Long brkQuantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status")
    private RequestStatus approvalStatus; //for each tool

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_crib")
    private User approvedByCrib;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_wpm")
    private User approvedByWpm;

    @Enumerated(EnumType.STRING)
    @Column(name = "return_status")
    private ReturnStatus returnStatus;

    @Column(name = "return_date")
    private LocalDateTime returnDate;

    @Column(name = "fine")
    private Long fine;
}
