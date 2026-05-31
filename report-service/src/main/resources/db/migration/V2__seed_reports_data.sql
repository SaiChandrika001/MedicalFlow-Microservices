INSERT INTO reports (patient_id, report_name, report_type, file_url, uploaded_date, storage_file_name)
VALUES
(1, 'Annual Physical Report', 'LAB_RESULT', 'https://medicalflow-reports.s3.amazonaws.com/reports/annual-physical-report.pdf', CURRENT_TIMESTAMP, 'annual-physical-report.pdf');
