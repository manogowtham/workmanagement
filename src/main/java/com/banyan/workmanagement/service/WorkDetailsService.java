package com.banyan.workmanagement.service;

import com.banyan.workmanagement.model.WorkDetails;
import com.banyan.workmanagement.repository.WorkDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class WorkDetailsService {

    @Autowired
    private WorkDetailsRepository workDetailsRepository;

    // Existing methods omitted for brevity...

    /**
     * Returns a page of WorkDetails filtered by optional parameters.
     */
    public Page<WorkDetails> findFilteredPage(
            String engineer,
            String customer,
            String status,
            LocalDate start,
            LocalDate end,
            Pageable pageable) {
        Specification<WorkDetails> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (engineer != null && !engineer.isBlank()) {
                predicates.add(cb.equal(root.get("engineerName"), engineer));
            }
            if (customer != null && !customer.isBlank()) {
                predicates.add(cb.equal(root.get("customerName"), customer));
            }
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (start != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("date"), start));
            }
            if (end != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("date"), end));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return workDetailsRepository.findAll(spec, pageable);
    }

    public List<WorkDetails> findFiltered(String engineer, String customer, String status, LocalDate start,
            LocalDate end) {
        Specification<WorkDetails> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (engineer != null && !engineer.isBlank()) {
                predicates.add(cb.equal(root.get("engineerName"), engineer));
            }
            if (customer != null && !customer.isBlank()) {
                predicates.add(cb.equal(root.get("customerName"), customer));
            }
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (start != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("date"), start));
            }
            if (end != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("date"), end));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return workDetailsRepository.findAll(spec);
    }

    public WorkDetails getWorkDetailById(Long workDetailId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getWorkDetailById'");
    }

    public byte[] generatePdfReport(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'generatePdfReport'");
    }

    public WorkDetails findById(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findById'");
    }
}
