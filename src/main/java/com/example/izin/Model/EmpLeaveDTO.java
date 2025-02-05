package com.example.izin.Model;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class EmpLeaveDTO {
    private String employeeName;
    private String employeeLastname;
    private String leaveReason;
    private String leaveApprover;
    private LocalDate leaveStart;
    private LocalDate leaveEnd;
}
