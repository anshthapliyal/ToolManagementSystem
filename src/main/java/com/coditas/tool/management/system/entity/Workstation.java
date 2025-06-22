package com.coditas.tool.management.system.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "workstations")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Workstation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "station_code", nullable = false, unique = true)
    private String stationCode;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", referencedColumnName = "id", unique = true)
    private User worker; // one-to-one with a Worker user

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workplace_id", nullable = false)
    private Workplace workplace;
}
