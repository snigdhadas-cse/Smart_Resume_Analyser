package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeRepository
        extends JpaRepository<ResumeData, Long> {

}