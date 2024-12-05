package com.example.izin.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
@Data
@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank(message = "Name cannot be blank")

    private String name;

    @NotBlank(message = "Lastname cannot be blank")
    private String lastname;

    @NotNull(message = "TCKN cannot be null")
    @Size(min = 11, max = 11, message = "TCKN must be 11 digits")
    private String tckn;

    @Past(message = "Birth date must be in the past")
    private LocalDate birthDate;

    @NotNull(message = "Date of employment cannot be null")

    private LocalDate dateOfEmployment;

    @NotNull(message = "Phone number cannot be null")
    @Column(name = "phone_number", nullable = false, length = 15)
    private String phoneNumber;

    @Column(name  = "Status")
    private String status ;
}
