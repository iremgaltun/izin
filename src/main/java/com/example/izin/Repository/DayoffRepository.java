package com.example.izin.Repository;

import com.example.izin.Model.Dayoff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DayoffRepository extends JpaRepository<Dayoff, Integer> {
}
