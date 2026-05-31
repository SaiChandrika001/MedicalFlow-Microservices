package com.medicalflow.appointmentservice.repository;

import com.medicalflow.appointmentservice.entity.Appointment;
import com.medicalflow.appointmentservice.entity.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    Page<Appointment> findByUserId(Long userId, Pageable pageable);

    Page<Appointment> findByDoctorId(Long doctorId, Pageable pageable);

    List<Appointment> findByStatus(AppointmentStatus status);

    @Query("SELECT a FROM Appointment a WHERE a.userId = :userId AND a.status = :status")
    Page<Appointment> findByUserIdAndStatus(@Param("userId") Long userId,
                                             @Param("status") AppointmentStatus status,
                                             Pageable pageable);

    @Query("SELECT a FROM Appointment a WHERE a.appointmentDate BETWEEN :startDate AND :endDate")
    List<Appointment> findAppointmentsBetween(@Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);

    @Query("SELECT a FROM Appointment a WHERE a.doctorId = :doctorId AND a.appointmentDate BETWEEN :startDate AND :endDate")
    List<Appointment> findDoctorAppointmentsBetween(@Param("doctorId") Long doctorId,
                                                     @Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate);

    Optional<Appointment> findByIdAndUserId(Long id, Long userId);

    boolean existsByDoctorIdAndAppointmentDateAndStatus(Long doctorId, LocalDateTime appointmentDate, AppointmentStatus status);
}
