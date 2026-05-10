package com.example.demo;

import java.util.List;

public class AnalysisResponse {

    private int score;
    private List<String> matchedSkills;
    private List<String> missingSkills;
    private String extractedText;

    public AnalysisResponse() {
    }

    public AnalysisResponse(
            int score,
            List<String> matchedSkills,
            List<String> missingSkills,
            String extractedText) {

        this.score = score;
        this.matchedSkills = matchedSkills;
        this.missingSkills = missingSkills;
        this.extractedText = extractedText;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public List<String> getMatchedSkills() {
        return matchedSkills;
    }

    public void setMatchedSkills(List<String> matchedSkills) {
        this.matchedSkills = matchedSkills;
    }

    public List<String> getMissingSkills() {
        return missingSkills;
    }

    public void setMissingSkills(List<String> missingSkills) {
        this.missingSkills = missingSkills;
    }

    public String getExtractedText() {
        return extractedText;
    }

    public void setExtractedText(String extractedText) {
        this.extractedText = extractedText;
    }
}