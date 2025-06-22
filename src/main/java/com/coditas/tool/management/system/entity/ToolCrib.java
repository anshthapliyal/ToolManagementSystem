package com.coditas.tool.management.system.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tool_cribs")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ToolCrib {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @OneToMany  //new table
    @JoinTable(
            name = "tool_crib_managers",
            joinColumns = @JoinColumn(name = "tool_crib_id"),
            inverseJoinColumns = @JoinColumn(name = "tool_crib_manager_id")
    )
    private List<User> toolCribManagers = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workplace_id")
    private Workplace workplace;

}
