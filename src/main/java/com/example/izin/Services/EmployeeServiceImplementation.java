package com.example.izin.Services;

import com.example.izin.Model.EmpLeaveDTO;
import com.example.izin.Model.Employee;
import com.example.izin.Model.Leave;
import com.example.izin.Repository.EmployeeRepository;
import com.example.izin.Repository.LeaveRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EmployeeServiceImplementation implements EmployeeService {

    @PersistenceContext
    private EntityManager entityManager;
    private final EmployeeRepository employeeRepository;
    @Autowired
    private final LeaveRepository leaveRepository;

    @Autowired
    public EmployeeServiceImplementation(EmployeeRepository employeeRepository, LeaveRepository leaveRepository) {
        this.employeeRepository = employeeRepository;
        this.leaveRepository = leaveRepository;
    }

    public List<EmpLeaveDTO> getAllEmployeeLeaveDetails() {
        return employeeRepository.getEmployeeLeaveDetails();
    }

    @Override

    public Employee add(Employee employee) {
        employeeRepository.findByTckn(employee.getTckn()).ifPresent(existing -> {
            throw new IllegalArgumentException("An employee with this TCKN already exists: " + employee.getTckn());
        });
        return employeeRepository.save(employee);
    }

    @Override
    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    @Override
    public Optional<Employee> findById(Long id) {
        return employeeRepository.findById(id);
    }


    @Override
    public Employee update(Employee employee) {
        if (employee.getId() == null) {
            throw new RuntimeException("Çalışan ID null olamaz!");
        }

        Optional<Employee> existingEmployeeOpt = employeeRepository.findById(employee.getId());

        if (existingEmployeeOpt.isEmpty()) {
            throw new RuntimeException("Employee not found with id: " + employee.getId());
        }

        Employee existingEmployee = existingEmployeeOpt.get();

        existingEmployee.setName(employee.getName());
        existingEmployee.setLastname(employee.getLastname());
        existingEmployee.setTckn(employee.getTckn());
        existingEmployee.setBirthDate(employee.getBirthDate());
        existingEmployee.setDateOfEmployment(employee.getDateOfEmployment());
        existingEmployee.setPhoneNumber(employee.getPhoneNumber());
        existingEmployee.setPosition(employee.getPosition());

        return employeeRepository.save(existingEmployee);
    }


    @Override
    public void delete(Employee employee) {
        if (employee == null || employee.getId() == null) {
            throw new IllegalArgumentException("Employee or ID cannot be null");
        }

        if (!employeeRepository.existsById(employee.getId())) {
            throw new RuntimeException("Employee not found with id: " + employee.getId());
        }

        employeeRepository.delete(employee);
    }

    @Override
    public boolean isEmployeeOnLeave(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Employee ID cannot be null");
        }

        LocalDate today = LocalDate.now();
        List<Leave> leaves = leaveRepository.findByEmployeeId(id);
        return leaves.stream().anyMatch(leave ->
                !today.isBefore(leave.getLeaveStart()) && !today.isAfter(leave.getLeaveEnd())
        );
    }
    @Override
    public Optional<Employee> findByTckn(String tckn) {
        return employeeRepository.findByTckn(tckn);
    }

}
