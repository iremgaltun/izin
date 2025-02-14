package com.example.izin.Services;

import com.example.izin.Model.EmpLeaveDTO;
import com.example.izin.Model.Employee;
import com.example.izin.Model.Leave;
import com.example.izin.Repository.LeaveRepository;
import com.example.izin.Services.LeaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class LeaveServiceImplemmentation implements LeaveService {

    private final LeaveRepository leaveRepository;
    private final EmployeeService employeeService;


    @Autowired
    public LeaveServiceImplemmentation(LeaveRepository leaveRepository, EmployeeService employeeService) {
        this.leaveRepository = leaveRepository;
        this.employeeService = employeeService;
    }

    @Override
    @Transactional
    public List<Leave> findAll() {
        return leaveRepository.findAll();
    }

    @Override
    @Transactional
    public Leave findById(long id) {
        return leaveRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void update(Leave leave) {
        leaveRepository.save(leave);
    }

    @Override
    @Transactional
    public void delete(Leave leave) {
        leaveRepository.delete(leave);
    }

    @Transactional
    public void deleteLeave(long id) {
        leaveRepository.deleteById(id);
    }

    @Transactional
    public void save(EmpLeaveDTO newLeaveDTO) {
        // EmployeeService örneğini kullanarak çalışanın bilgilerini alın
        Optional<Employee> optionalEmployee = employeeService.findByTckn(newLeaveDTO.getTckn());

        // Eğer employee varsa, işlemi gerçekleştirin
        if (optionalEmployee.isPresent()) {
            Employee employee = optionalEmployee.get(); // Employee nesnesini alıyoruz
            Leave leave = new Leave();
            leave.setLeaveReason(newLeaveDTO.getLeaveReason());
            leave.setLeaveApprover(newLeaveDTO.getLeaveApprover());
            leave.setLeaveStart(newLeaveDTO.getLeaveStart());
            leave.setLeaveEnd(newLeaveDTO.getLeaveEnd());
            leave.setEmployee(employee); // Employee ile ilişkilendir

            // Kaydetme işlemi
            try {
                leaveRepository.save(leave);
            } catch (Exception e) {
                throw new IllegalStateException("İzin kaydedilirken hata oluştu: " + e.getMessage());
            }
        } else {
            throw new IllegalArgumentException("Çalışan bulunamadı!");
        }
    }



}

