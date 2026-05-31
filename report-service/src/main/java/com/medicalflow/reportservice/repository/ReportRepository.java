package com.medicalflow.reportservice.repository;

import com.medicalflow.reportservice.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findByPatientId(Long patientId);
}
