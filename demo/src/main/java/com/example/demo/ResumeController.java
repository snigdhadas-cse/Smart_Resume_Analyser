package com.example.demo;

import java.util.*;
import java.nio.file.*;

import org.apache.tika.Tika;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin(origins = "*")
@RestController
public class ResumeController {

        private final ResumeRepository repository;

        public ResumeController(ResumeRepository repository) 
        {
            this.repository = repository;
        }

    // -----------------------------
    // DYNAMIC SKILL EXTRACTION
    // -----------------------------
    private List<String> extractSkills(String text, String skillsInput) {

        List<String> foundSkills = new ArrayList<>();

        String lowerText = text.toLowerCase();

        // Convert user input into skill list
        List<String> requiredSkills = Arrays.stream(skillsInput.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .toList();

        // Match skills
        for (String skill : requiredSkills) {

            if (lowerText.contains(skill)) {
                foundSkills.add(skill);
            }
        }

        return foundSkills;
    }

    
    // -----------------------------
    // RESUME MATCH API
    // -----------------------------
    @PostMapping("/match")
    public Map<String, Object> matchResume(

            @RequestParam("file") MultipartFile file,

            @RequestParam("skills") String skillsInput

    ) {

        Map<String, Object> result = new HashMap<>();

        try {

            // Extract resume text
            Tika tika = new Tika();

            String resumeText = tika.parseToString(file.getInputStream());

            // Extract matched skills
            List<String> matchedSkills =
                    extractSkills(resumeText, skillsInput);

            // Required skills list
            List<String> requiredSkills = Arrays.stream(skillsInput.split(","))
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .toList();

            // Missing skills
            List<String> missingSkills = new ArrayList<>();

            for (String skill : requiredSkills) {

                if (!matchedSkills.contains(skill)) {
                    missingSkills.add(skill);
                }
            }

            // Score calculation
            double score = 0;

            if (requiredSkills.size() > 0) {

                score = ((double) matchedSkills.size()
                        / requiredSkills.size()) * 100;
            }

            score = Math.round(score * 100.0) / 100.0;

            // Recommendation
            String message;

            if (score >= 75) {
                message = "Strong Match";
            }
            else if (score >= 50) {
                message = "Moderate Match";
            }
            else {
                message = "Needs Improvement";
            }

            // Return JSON response
            result.put("matchScore", score);

            result.put("matchedSkills", matchedSkills);

            result.put("missingSkills", missingSkills);

            result.put("recommendation", message);

        }

        catch (Exception e) {

            result.put("error", "Processing failed");

            e.printStackTrace();
        }

        ResumeData data = new ResumeData();

        data.setFileName(file.getOriginalFilename());
        data.setMatchScore(score);

        data.setMatchedSkills(
            String.join(", ", resumeSkills)
        );

        data.setMissingSkills(
            String.join(", ", missing)
        );

        data.setRecommendation(message);

        repository.save(data);

        return result;
    }

    // -----------------------------
    // FILE SAVE API
    // -----------------------------
    @PostMapping("/upload")
    public String uploadFile(
            @RequestParam("file") MultipartFile file) {

        try {

            Path uploadPath = Paths.get("uploads");

            if (!Files.exists(uploadPath)) {

                Files.createDirectories(uploadPath);
            }

            Path filePath =
                    uploadPath.resolve(file.getOriginalFilename());

            Files.copy(
                    file.getInputStream(),
                    filePath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            return "File saved: "
                    + file.getOriginalFilename();

        }

        catch (Exception e) {

            return "Error saving file";
        }
    }

    @GetMapping("/history")
    public List<ResumeData> getResumeHistory() {

        return repository.findAll();

    }
}