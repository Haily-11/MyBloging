package com.blog.mylog.service;

import com.blog.mylog.entity.CheckIn;
import com.blog.mylog.repository.CheckInRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CheckInService  {
    private final CheckInRepository checkInRepository;

    public CheckIn checkIn() {
        LocalDate today = LocalDate.now();
        CheckIn todayCheckIn = checkInRepository.findByCheckInDate(today);

        if (todayCheckIn != null) {
            return todayCheckIn;
        }

        List<CheckIn> recentCheckIns = checkInRepository.findRecentCheckIns();
        int streak = 1;

        if (!recentCheckIns.isEmpty()) {
            CheckIn lastCheckIn = recentCheckIns.get(0);
            long daysBetween = ChronoUnit.DAYS.between(lastCheckIn.getCheckInDate(), today);

            if (daysBetween == 1) {
                streak = lastCheckIn.getStreak() + 1;
            }
        }

        CheckIn checkIn = new CheckIn();
        checkIn.setCheckInDate(today);
        checkIn.setStreak(streak);
        return checkInRepository.save(checkIn);
    }

    public List<CheckIn> getRecentCheckIns() {
        return checkInRepository.findRecentCheckIns();
    }

    public CheckIn getTodayCheckIn() {
        return checkInRepository.findByCheckInDate(LocalDate.now());
    }
}
