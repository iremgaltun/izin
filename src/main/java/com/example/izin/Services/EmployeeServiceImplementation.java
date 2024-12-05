package com.example.izin.Services;

import com.example.izin.Model.Employee;
import com.example.izin.Repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeServiceImplementation implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    @Autowired
    public EmployeeServiceImplementation(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public void save(Employee employee) {
        employeeRepository.save(employee); // Hem ekleme hem güncelleme işlemi yapılabilir.
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
    public Employee add(Employee employee) {
        return employeeRepository.save(employee); // Yeni çalışan ekler
    }

    @Override
    @Transactional
    public Employee update(Employee employee) {
        return employeeRepository.findById(employee.getId())
                .map(existing -> {
                    existing.setName(employee.getName());
                    existing.setLastname(employee.getLastname());
                    // Diğer alanlar...
                    return employeeRepository.save(existing);
                })
                .orElseThrow(() -> new IllegalArgumentException("Çalışan bulunamadı."));
    }


    @Override
    public void delete(Employee employee) {
        employeeRepository.delete(employee); // Çalışanı siler
    }
}
