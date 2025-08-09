package com.banyan.workmanagement.service;

import com.banyan.workmanagement.model.Customer;
import com.banyan.workmanagement.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Override
    public Customer saveCustomer(Customer newCustomer) {
        // Removed markRegularCustomers call as per request
        return customerRepository.save(newCustomer);
    }

    @Override
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @Override
    public void deleteCustomer(Long id) {
        customerRepository.deleteById(id);
    }

    @Override
    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id).orElse(null);
    }

    @Override
    public void save(Customer customer) {
        customerRepository.save(customer);
    }

    @Override
    public Customer findById(Long id) {
        return customerRepository.findById(id).orElse(null);
    }

    @Override
    public List<Customer> searchCustomers(String search) {
        return customerRepository.searchCustomers(search);
    }

    // Removed method findByCompanyName as per request
    // Removed method markRegularCustomers as per request

    @Override
    public List<String> getLocationsForCustomer(Customer customer) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getLocationsForCustomer'");
    }

    @Override
    public Page<Customer> getCustomersPaged(Pageable pageable) {
        return customerRepository.findAll(pageable);
    }

    @Override
    public Page<Customer> searchCustomersPaged(String search, Pageable pageable) {
        return customerRepository.searchCustomersPaged(search, pageable);
    }

}
