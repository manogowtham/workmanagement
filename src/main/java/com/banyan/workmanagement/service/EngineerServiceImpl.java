package com.banyan.workmanagement.service;

import com.banyan.workmanagement.model.Engineer;
import com.banyan.workmanagement.repository.EngineerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EngineerServiceImpl implements EngineerService {

    @Autowired
    private EngineerRepository engineerRepository;

    @Override
    public List<Engineer> getAllEngineers() {
        return engineerRepository.findAll();
    }

    @Override
    public Engineer getEngineerById(Long id) {
        Optional<Engineer> engineer = engineerRepository.findById(id);
        return engineer.orElseThrow(() -> new RuntimeException("Engineer not found with id: " + id));
    }

    @Override
    public Engineer saveEngineer(Engineer engineer) {
        return engineerRepository.save(engineer);
    }

    @Override
    public Engineer updateEngineer(Engineer engineer) {
        return engineerRepository.save(engineer);
    }

    @Override
    public void deleteEngineerById(Long id) {
        engineerRepository.deleteById(id);
    }
}
