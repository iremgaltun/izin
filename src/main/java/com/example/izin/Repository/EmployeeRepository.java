package com.example.izin.Repository;

import com.example.izin.Model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee,Long> {
    // You can define custom queries here if necessary, otherwise JpaRepository methods will suffice
}
