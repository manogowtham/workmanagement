package com.banyan.workmanagement.service;

import com.banyan.workmanagement.model.Customer;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerService {

    Customer saveCustomer(Customer customer); // This should now include regular customer logic

    List<Customer> getAllCustomers();

    List<Customer> searchCustomers(String search);

    void deleteCustomer(Long id);

    Customer getCustomerById(Long id);

    void save(Customer customer); // Optional — consider merging this into saveCustomer()

    Customer findById(Long id);

    // Removed methods related to regular customer detection as per request
    // List<Customer> findByCompanyName(String companyName);

    // void markRegularCustomers(List<Customer> customers);

    List<String> getLocationsForCustomer(Customer customer);

    // New method for paging support
    Page<Customer> getCustomersPaged(Pageable pageable);

    // New method for paged search
    Page<Customer> searchCustomersPaged(String search, Pageable pageable);

}
