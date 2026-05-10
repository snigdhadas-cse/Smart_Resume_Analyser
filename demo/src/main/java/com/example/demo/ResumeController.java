package com.example.demo;

import java.util.*;
import java.nio.file.*;
import java.util.stream.Collectors;

import org.apache.tika.Tika;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin(origins = "*")
@RestController
public class ResumeController {

    private final ResumeRepository repository;

    public ResumeController(ResumeRepository repository) {
        this.repository = repository;
    }

    // DYNAMIC SKILL EXTRACTION
    private List<String> extractSkills(
            String text,
            List<String> skillsList) {

        List<String> foundSkills =
                new ArrayList<>();

        String lowerText =
                text.toLowerCase();

        for (String skill : skillsList) {

            if (lowerText.contains(
                    skill.toLowerCase())) {

                foundSkills.add(skill);
            }
        }

        return foundSkills;
    }

    // MAIN RESUME MATCH API
    @PostMapping("/match")
    public Map<String, Object> matchResume(
            @RequestParam("file") MultipartFile file,
            @RequestParam("skills") String skillsInput,
            @RequestParam("jobDescription") String jobDesc) {

        Map<String, Object> result =
                new HashMap<>();

        try {

            Tika tika = new Tika();

            String resumeText =
                    tika.parseToString(
                            file.getInputStream());

            // CUSTOM SKILLS FROM USER INPUT
            List<String> customSkills =
                    Arrays.stream(
                                    skillsInput.split(","))
                            .map(String::trim)
                            .map(String::toLowerCase)
                            .collect(Collectors.toList());

            // EXTRACT RESUME SKILLS
            List<String> resumeSkills =
                    extractSkills(
                            resumeText,
                            customSkills);

            // EXTRACT JOB SKILLS
            List<String> jobSkills =
                    extractSkills(
                            jobDesc,
                            customSkills);

            // MATCH CALCULATION
            int matchCount = 0;

            for (String skill : jobSkills) {

                if (resumeSkills.contains(skill)) {
                    matchCount++;
                }
            }

            double score = 0;

            if (jobSkills.size() > 0) {

                score =
                        (double) matchCount
                                / jobSkills.size()
                                * 100;
            }

            score =
                    Math.round(score * 100.0)
                            / 100.0;

            // RECOMMENDATION
            String message;

            if (score >= 75) {

                message = "Strong match";
            }

            else if (score >= 50) {

                message = "Moderate match";
            }

            else {

                message = "Needs improvement";
            }

            // MISSING SKILLS
            List<String> missing =
                    new ArrayList<>();

            for (String skill : jobSkills) {

                if (!resumeSkills.contains(skill)) {

                    missing.add(skill);
                }
            }

            // RESPONSE
            result.put(
                    "matchScore",
                    score);

            result.put(
                    "resumeSkills",
                    resumeSkills);

            result.put(
                    "missingSkills",
                    missing);

            result.put(
                    "recommendation",
                    message);

            // SAVE TO DATABASE
            ResumeData data =
                    new ResumeData();

            data.setFileName(
                    file.getOriginalFilename());

            data.setMatchScore(score);

            data.setMatchedSkills(
                    String.join(
                            ", ",
                            resumeSkills));

            data.setMissingSkills(
                    String.join(
                            ", ",
                            missing));

            data.setRecommendation(
                    message);

            repository.save(data);

        }

        catch (Exception e) {

            result.put(
                    "error",
                    "Processing failed");
        }

        return result;
    }

    @GetMapping("/")
    public String home() {
        return "Smart Resume Analyzer Running";
    }
    
    // FILE UPLOAD API
    @PostMapping("/upload")
    public String uploadFile(
            @RequestParam("file")
            MultipartFile file) {

        try {

            Path uploadPath =
                    Paths.get("uploads");

            if (!Files.exists(uploadPath)) {

                Files.createDirectories(
                        uploadPath);
            }

            Path filePath =
                    uploadPath.resolve(
                            file.getOriginalFilename());

            Files.copy(
                    file.getInputStream(),
                    filePath,
                    StandardCopyOption
                            .REPLACE_EXISTING);

            return "File saved: "
                    + file.getOriginalFilename();

        }

        catch (Exception e) {

            return "Error saving file";
        }
    }

    // HISTORY API
    @GetMapping("/history")
    public List<ResumeData> getResumeHistory() {

        return repository.findAll();
    }
}