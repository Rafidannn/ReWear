package com.example.application.service.user;

import com.example.application.model.user.School;
import com.example.application.repository.user.SchoolRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SchoolService {

    private final SchoolRepository schoolRepository;

    public SchoolService(SchoolRepository schoolRepository) {
        this.schoolRepository = schoolRepository;
    }

    public List<School> findAll() {
        return schoolRepository.findAll();
    }

    public Optional<School> findById(Long id) {
        return schoolRepository.findById(id);
    }

    public School save(School school) {
        return schoolRepository.save(school);
    }
}
