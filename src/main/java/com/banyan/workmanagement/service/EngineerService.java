package com.banyan.workmanagement.service;

import com.banyan.workmanagement.model.Engineer;

import java.util.List;

public interface EngineerService {
    List<Engineer> getAllEngineers();

    Engineer getEngineerById(Long id);

    Engineer saveEngineer(Engineer engineer);

    Engineer updateEngineer(Engineer engineer);

    void deleteEngineerById(Long id);
}
