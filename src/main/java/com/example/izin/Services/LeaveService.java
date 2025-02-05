package com.example.izin.Services;

import com.example.izin.Model.Leave;
import java.util.List;

public interface LeaveService {

    List<Leave> findAll();  // Tüm izinleri döner.

    Leave findById(long id);  // Belirli bir izin ID'si ile izin bilgisi döner.


    void saveOrUpdate(Leave leave);

    void update(Leave leave);

    void delete(Leave leave);

}
