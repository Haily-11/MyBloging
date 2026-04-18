package com.blog.mylog.repository;

import com.blog.mylog.entity.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CheckInRepository extends JpaRepository<CheckIn, Long> {

    CheckIn findByCheckInDate(LocalDate date);

    @Query("SELECT c FROM CheckIn c ORDER BY c.checkInDate DESC")
    List<CheckIn> findRecentCheckIns();
}