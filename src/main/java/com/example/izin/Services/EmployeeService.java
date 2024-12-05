package com.example.izin.Services;

import com.example.izin.Model.Employee;

import java.util.List;
import java.util.Optional;

public interface EmployeeService {

    // Çalışanı kaydet (ekle veya güncelle)
    void save(Employee employee);

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
}
