package com.example.izin.Services;

import com.example.izin.Model.Leave;
import com.example.izin.Repository.LeaveRepository;
import com.example.izin.Services.LeaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class LeaveServiceImplemmentation implements LeaveService {


    private final LeaveRepository leaveRepository;

    @Autowired
    public LeaveServiceImplemmentation(LeaveRepository leaveRepository) {
        this.leaveRepository = leaveRepository;
    }



    @Override
    public List<Leave> findAll() {
        return leaveRepository.findAll();
    }

    @Override
    public Leave findById(long id) {
        return leaveRepository.findById(id).orElse(null);
    }


    @Override
    public void saveOrUpdate(Leave leave) {
        if (leave.getId() == 0) {
            leaveRepository.save(leave);  // Yeni izin ekler
        } else {
            leaveRepository.save(leave);  // Mevcut izni günceller
        }
    }

    @Override
    public void update(Leave leave) {
        leaveRepository.save(leave);
    }


    public void delete(Leave leave) {
        leaveRepository.delete(leave);
    }


    public void deleteLeave(long id) {
        leaveRepository.deleteById(id);
    }




}

