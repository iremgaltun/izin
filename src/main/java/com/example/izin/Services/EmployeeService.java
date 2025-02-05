package com.example.izin.Services;

import com.example.izin.Model.EmpLeaveDTO;
import com.example.izin.Model.Employee;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.Optional;

public interface EmployeeService {


    public List<EmpLeaveDTO> getAllEmployeeLeaveDetails();

    // Çalışanı kaydet (ekle veya güncelle)
    Employee save(Employee employee);

    // Tüm çalışanları getir
    List<Employee> findAll();

    // ID ile çalışan bul
    Optional<Employee> findById(Long id);

    // Çalışan ekle
    Employee add(Employee employee);

    // Çalışan güncelle
    Employee update(Employee employee);

    // Çalışan sil
    void delete(Employee employee);

    boolean isEmployeeOnLeave(Long id);
}
