package com.example.izin.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "dayoff")
public class Leave {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @NotBlank(message = "must declare reason")
    @Column(name = "leave_reason")
    private String leaveReason;

    @NotBlank
    @Column(name = "leave_approver")
    private String leaveApprover;

    @NotNull
    @Column(name = "leave_start")
    private LocalDate leaveStart;

    @NotNull
    @Column(name = "leave_end")
    private LocalDate leaveEnd;

    @ManyToOne
    @JoinColumn(name = "emp_id", nullable = false) // Foreign key: Employee tablosuna bağlanacak
    private Employee employee;
}
