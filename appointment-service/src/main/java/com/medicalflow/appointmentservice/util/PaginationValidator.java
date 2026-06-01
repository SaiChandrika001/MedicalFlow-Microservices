package com.medicalflow.appointmentservice.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PaginationValidator {

    public static final int MAX_PAGE_SIZE = 100;
    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_PAGE_SIZE = 20;

    public static Pageable validateAndBuildPageable(int page, int size) {
        return validateAndBuildPageable(page, size, "createdAt", Sort.Direction.DESC);
    }

    public static Pageable validateAndBuildPageable(int page, int size, String sortBy, Sort.Direction direction) {
        if (page < 0) {
            throw new IllegalArgumentException("Page number must be >= 0");
        }
        if (size <= 0 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("Page size must be between 1 and " + MAX_PAGE_SIZE);
        }

        if (sortBy != null && !sortBy.isEmpty()) {
            return PageRequest.of(page, size, Sort.by(direction, sortBy));
        }

        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    }
}
