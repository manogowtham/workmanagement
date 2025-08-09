package com.banyan.workmanagement.repository;

import com.banyan.workmanagement.model.Attendance;
import com.banyan.workmanagement.model.Engineer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    // Find attendance by engineer and date
    Optional<Attendance> findByEngineerAndAttendanceDate(Engineer engineer, LocalDate attendanceDate);

    // Find all attendance records for a specific engineer
    List<Attendance> findByEngineerOrderByAttendanceDateDesc(Engineer engineer);

    // Find attendance records for a specific engineer within date range
    List<Attendance> findByEngineerAndAttendanceDateBetweenOrderByAttendanceDateDesc(
            Engineer engineer, LocalDate startDate, LocalDate endDate);

    // Find all attendance records for a specific date
    List<Attendance> findByAttendanceDateOrderByEngineerName(LocalDate attendanceDate);

    // Find attendance records within date range
    List<Attendance> findByAttendanceDateBetweenOrderByAttendanceDateDescEngineerNameAsc(
            LocalDate startDate, LocalDate endDate);

    // Find attendance records by status
    List<Attendance> findByStatusOrderByAttendanceDateDesc(String status);

    // Find attendance records by engineer and status within date range
    List<Attendance> findByEngineerAndStatusAndAttendanceDateBetween(
            Engineer engineer, String status, LocalDate startDate, LocalDate endDate);

    // Custom queries for reporting
    @Query("SELECT a FROM Attendance a WHERE a.engineer.id = :engineerId AND a.attendanceDate BETWEEN :startDate AND :endDate ORDER BY a.attendanceDate DESC")
    List<Attendance> findAttendanceByEngineerAndDateRange(
            @Param("engineerId") Long engineerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Get overtime records
    @Query("SELECT a FROM Attendance a WHERE a.overtimeMinutes > 0 AND a.attendanceDate BETWEEN :startDate AND :endDate ORDER BY a.attendanceDate DESC")
    List<Attendance> findOvertimeRecords(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Get overtime records for specific engineer
    @Query("SELECT a FROM Attendance a WHERE a.engineer.id = :engineerId AND a.overtimeMinutes > 0 AND a.attendanceDate BETWEEN :startDate AND :endDate ORDER BY a.attendanceDate DESC")
    List<Attendance> findOvertimeRecordsByEngineer(
            @Param("engineerId") Long engineerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Calculate total work hours for engineer in date range
    @Query("SELECT COALESCE(SUM(a.totalWorkMinutes), 0) FROM Attendance a WHERE a.engineer.id = :engineerId AND a.attendanceDate BETWEEN :startDate AND :endDate")
    Integer getTotalWorkMinutesByEngineerAndDateRange(
            @Param("engineerId") Long engineerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Calculate total overtime hours for engineer in date range
    @Query("SELECT COALESCE(SUM(a.overtimeMinutes), 0) FROM Attendance a WHERE a.engineer.id = :engineerId AND a.attendanceDate BETWEEN :startDate AND :endDate")
    Integer getTotalOvertimeMinutesByEngineerAndDateRange(
            @Param("engineerId") Long engineerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Count attendance days by status for engineer in date range
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.engineer.id = :engineerId AND a.status = :status AND a.attendanceDate BETWEEN :startDate AND :endDate")
    Long countAttendanceByEngineerStatusAndDateRange(
            @Param("engineerId") Long engineerId,
            @Param("status") String status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Get attendance statistics for all engineers in date range
    @Query("SELECT a.engineer.name, COUNT(a), COALESCE(SUM(a.totalWorkMinutes), 0), COALESCE(SUM(a.overtimeMinutes), 0) "
            +
            "FROM Attendance a WHERE a.attendanceDate BETWEEN :startDate AND :endDate " +
            "GROUP BY a.engineer.id, a.engineer.name ORDER BY a.engineer.name")
    List<Object[]> getAttendanceStatistics(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Find recent attendance records (last 30 days)
    @Query("SELECT a FROM Attendance a WHERE a.attendanceDate >= :fromDate ORDER BY a.attendanceDate DESC, a.engineer.name ASC")
    List<Attendance> findRecentAttendance(@Param("fromDate") LocalDate fromDate);

    // Get distinct engineer names from attendance records
    @Query("SELECT DISTINCT a.engineer.name FROM Attendance a ORDER BY a.engineer.name")
    List<String> findDistinctEngineerNames();

    // Find attendance records that are linked to work details
    @Query("SELECT a FROM Attendance a WHERE a.workDetails IS NOT NULL ORDER BY a.attendanceDate DESC")
    List<Attendance> findAttendanceWithWorkDetails();

    // Find attendance records without work details
    @Query("SELECT a FROM Attendance a WHERE a.workDetails IS NULL ORDER BY a.attendanceDate DESC")
    List<Attendance> findAttendanceWithoutWorkDetails();
}