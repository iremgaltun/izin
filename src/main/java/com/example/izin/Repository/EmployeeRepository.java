package com.example.izin.Repository;
import com.example.izin.Model.EmpLeaveDTO;
import com.example.izin.Model.Leave; // Örnek paket adı

import com.example.izin.Model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee,Long> {
    Optional<Employee> findByTckn(String tckn);

    @Query("SELECT new com.example.izin.Model.EmpLeaveDTO(e.name, e.lastname, l.leaveReason, l.leaveApprover, l.leaveStart, l.leaveEnd) " +
            "FROM Employee e JOIN e.leaves l " +
            "ORDER BY e.name, l.leaveStart")
    List<EmpLeaveDTO> getEmployeeLeaveDetails();

}
