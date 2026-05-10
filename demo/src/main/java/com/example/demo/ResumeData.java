package com.example.demo;

import jakarta.persistence.*;

@Entity
public class ResumeData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    private double matchScore;

    @Column(length = 2000)
    private String matchedSkills;

    @Column(length = 2000)
    private String missingSkills;

    private String recommendation;

    public Long getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public double getMatchScore() {
        return matchScore;
    }

    public String getMatchedSkills() {
        return matchedSkills;
    }

    public String getMissingSkills() {
        return missingSkills;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setMatchScore(double matchScore) {
        this.matchScore = matchScore;
    }

    public void setMatchedSkills(String matchedSkills) {
        this.matchedSkills = matchedSkills;
    }

    public void setMissingSkills(String missingSkills) {
        this.missingSkills = missingSkills;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }
}