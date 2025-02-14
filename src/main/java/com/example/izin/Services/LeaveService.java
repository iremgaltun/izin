package com.example.izin.Services;

import com.example.izin.Model.EmpLeaveDTO;
import com.example.izin.Model.Leave;
import java.util.List;

public interface LeaveService {


    void save(EmpLeaveDTO newLeave); // Parametre olarak EmpLeaveDTO alır.

    List<Leave> findAll();  // Tüm izinleri döner.

    Leave findById(long id);  // Belirli bir izin ID'si ile izin bilgisi döner.

    void update(Leave leave);

    void delete(Leave leave);
}
