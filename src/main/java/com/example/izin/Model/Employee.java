package com.example.izin.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name cannot be blank")
    private String name;

    @NotBlank(message = "Lastname cannot be blank")
    private String lastname;

    @NotNull(message = "TCKN cannot be null")
    @Pattern(regexp = "\\d{11}", message = "TCKN must be exactly 11 digits") // rakam kontrl
    private String tckn;

    @Past(message = "Birth date must be in the past")
    private LocalDate birthDate;

    @NotNull(message = "Date of employment cannot be null")
    private LocalDate dateOfEmployment;

    @NotNull(message = "Phone number cannot be null")
    @Pattern(regexp = "\\d{10,15}", message = "Phone number must be between 10 and 15 digits")
    @Column(name = "phone_number", nullable = false, length = 15)
    private String phoneNumber;

    @Column(name = "position")
    private String position;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.MERGE, orphanRemoval = true) // İzinle silinmicek çalışan silise bile mergle
    private List<Leave> leaves;
}
