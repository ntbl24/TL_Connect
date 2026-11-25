package com.tl_connect.dev.modules.attendance.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.google.firebase.database.DatabaseException;
import com.tl_connect.dev.modules.attendance.entity.Attendance;
import com.tl_connect.dev.modules.attendance.repository.AttendanceRepository;
import com.tl_connect.dev.modules.attendance.service.interfaces.AttendanceService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    @Override
    public boolean existsBySessionIdAndStudentId(String sessionId, Long studentId) {
        return attendanceRepository.existsBySessionIdAndStudentId(sessionId, studentId);
    }

    @Override
    public void save(Attendance attendance) {
        try {
            attendanceRepository.save(attendance);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Failed to save attendance: " + e.getMessage());
        }
    }
}
