package com.example.izin.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Data
@Entity
@Table(name= "dayoff")
public class Dayoff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String type;
    private LocalDate start;
    private LocalDate end ;
    private int emp_id;
}
