package com.banyan.workmanagement.repository;

import com.banyan.workmanagement.model.Customer;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findAllByOrderByIdDesc(); // Fetch customers sorted by ID in descending order

    @Query("SELECT c FROM Customer c WHERE " +
            "LOWER(c.vendorType) LIKE %:search% OR " +
            "LOWER(c.companyName) LIKE %:search% OR " +
            "LOWER(c.contactPerson) LIKE %:search% OR " +
            "LOWER(c.mobileNumber) LIKE %:search% OR " +
            "LOWER(c.address) LIKE %:search% OR " +
            "LOWER(c.area) LIKE %:search% OR " +
            "LOWER(c.state) LIKE %:search% OR " +
            "LOWER(c.city) LIKE %:search% OR " +
            "LOWER(c.pincode) LIKE %:search% OR " +
            "LOWER(c.gstNumber) LIKE %:search% OR " +
            "LOWER(c.companyContact) LIKE %:search% OR " +
            "LOWER(c.email) LIKE %:search%")
    List<Customer> searchCustomers(@Param("search") String search);

    @Query("SELECT c FROM Customer c WHERE " +
            "LOWER(c.vendorType) LIKE %:search% OR " +
            "LOWER(c.companyName) LIKE %:search% OR " +
            "LOWER(c.contactPerson) LIKE %:search% OR " +
            "LOWER(c.mobileNumber) LIKE %:search% OR " +
            "LOWER(c.address) LIKE %:search% OR " +
            "LOWER(c.area) LIKE %:search% OR " +
            "LOWER(c.state) LIKE %:search% OR " +
            "LOWER(c.city) LIKE %:search% OR " +
            "LOWER(c.pincode) LIKE %:search% OR " +
            "LOWER(c.gstNumber) LIKE %:search% OR " +
            "LOWER(c.companyContact) LIKE %:search% OR " +
            "LOWER(c.email) LIKE %:search%")
    org.springframework.data.domain.Page<Customer> searchCustomersPaged(@Param("search") String search,
            org.springframework.data.domain.Pageable pageable);

    @Query("SELECT DISTINCT c.city FROM Customer c")
    List<String> findDistinctCities();

    List<Customer> findByCompanyName(String companyName);

    List<Customer> findByCompanyNameIgnoreCase(String companyName);

}
