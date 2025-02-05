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
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class EmployeeServiceImplementation implements EmployeeService {



    @PersistenceContext
    private EntityManager entityManager;
    private final EmployeeRepository employeeRepository;
    private final LeaveRepository leaveRepository;

    @Autowired
    public EmployeeServiceImplementation(EmployeeRepository employeeRepository, LeaveRepository leaveRepository) {
        this.employeeRepository = employeeRepository;
        this.leaveRepository = leaveRepository;
    }
    public List<EmpLeaveDTO> getAllEmployeeLeaveDetails() {
        // EmployeeLeaveDTO listesini döndürür
        return employeeRepository.getEmployeeLeaveDetails();
    }


    @Override
    public Employee add(Employee employee) {
        // Eğer aynı TCKN'ye sahip bir çalışan zaten varsa, hata fırlat
        employeeRepository.findByTckn(employee.getTckn()).ifPresent(existing -> {
            throw new IllegalArgumentException("An employee with this TCKN already exists: " + employee.getTckn());
        });

        // Eğer TCKN benzersizse, çalışanı ekle
        return employeeRepository.save(employee);
    }
    @Override
    public Employee save(Employee employee) {
        if (employee.getId() == 0) {
            // Yeni bir çalışan ekleniyor
            return employeeRepository.save(employee);
        } else {
            // Güncelleme işlemi
            return employeeRepository.findById(employee.getId())
                    .map(existing -> {
                        existing.setName(employee.getName());
                        existing.setLastname(employee.getLastname());
                        existing.setTckn(employee.getTckn());
                        existing.setBirthDate(employee.getBirthDate());
                        existing.setDateOfEmployment(employee.getDateOfEmployment());
                        existing.setPhoneNumber(employee.getPhoneNumber());
                        existing.setPosition(employee.getPosition());
                        return employeeRepository.save(existing);
                    })
                    .orElseThrow(() -> new RuntimeException("Employee not found with id: " + employee.getId()));
        }
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
    @Transactional
    public Employee update(Employee employee) {
        Employee existingEmployee = entityManager.find(Employee.class, employee.getId());

        if (existingEmployee != null) {
            existingEmployee.setName(employee.getName());
            existingEmployee.setLastname(employee.getLastname());
            existingEmployee.setTckn(employee.getTckn());
            existingEmployee.setBirthDate(employee.getBirthDate());
            existingEmployee.setDateOfEmployment(employee.getDateOfEmployment());
            existingEmployee.setPhoneNumber(employee.getPhoneNumber());
            existingEmployee.setPosition(employee.getPosition());
            entityManager.merge(existingEmployee);  // Güncellenmiş entity'yi veritabanına kaydeder
            return existingEmployee;
        } else {
            throw new RuntimeException("Employee not found with id: " + employee.getId());
        }
    }

    @Override
    public void delete(Employee employee) {
        if (employeeRepository.existsById(employee.getId())) {
            employeeRepository.delete(employee);
        } else {
            throw new RuntimeException("Employee not found with id: " + employee.getId());
        }
    }



    @Override
    public boolean isEmployeeOnLeave(Long id) {
        LocalDate today = LocalDate.now();
        List<Leave> leaves = leaveRepository.findByEmployeeId(id);
        return leaves.stream().anyMatch(leave ->
                !today.isBefore(leave.getLeaveStart()) && !today.isAfter(leave.getLeaveEnd())
        );
    }



}
