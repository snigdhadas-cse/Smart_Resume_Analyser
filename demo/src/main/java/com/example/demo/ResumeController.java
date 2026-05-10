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
    private List<String> extractSkills(String text, List<String> skillsList) {

    List<String> foundSkills = new ArrayList<>();

    String lowerText = text.toLowerCase();

    for (String skill : skillsList) {

        if (lowerText.contains(skill.toLowerCase())) {
            foundSkills.add(skill);
        }
    }

    return foundSkills;
}
    @GetMapping("/health")
    public String health() {
        return "OK";
    }

    // MAIN RESUME MATCH API
    @PostMapping("/match")
public Map<String, Object> matchResume(
        @RequestParam("file") MultipartFile file,
        @RequestParam("skills") String skillsInput,
        @RequestParam("jobDescription") String jobDesc) {

    Map<String, Object> result = new HashMap<>();

    try {

        // ===== FILE CHECK =====
        if (file.isEmpty()) {
            result.put("error", "No file uploaded");
            return result;
        }

        // ===== EXTRACT TEXT =====
        Tika tika = new Tika();

        String resumeText = tika.parseToString(file.getInputStream());

        System.out.println("===== RESUME TEXT =====");
        System.out.println(resumeText);

        // ===== SKILLS LIST =====
        List<String> skillList = Arrays.stream(skillsInput.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        // ===== EXTRACT SKILLS =====
        List<String> resumeSkills = extractSkills(resumeText, skillList);

        List<String> jobSkills = extractSkills(jobDesc, skillList);

        // ===== MATCH SCORE =====
        int matchCount = 0;

        for (String skill : jobSkills) {
            if (resumeSkills.contains(skill)) {
                matchCount++;
            }
        }

        double score = 0;

        if (!jobSkills.isEmpty()) {
            score = ((double) matchCount / jobSkills.size()) * 100;
        }

        score = Math.round(score * 100.0) / 100.0;

        // ===== RECOMMENDATION =====
        String message;

        if (score >= 75) {
            message = "Strong Match";
        } else if (score >= 50) {
            message = "Moderate Match";
        } else {
            message = "Needs Improvement";
        }

        // ===== MISSING SKILLS =====
        List<String> missingSkills = new ArrayList<>();

        for (String skill : jobSkills) {
            if (!resumeSkills.contains(skill)) {
                missingSkills.add(skill);
            }
        }

        // ===== SAVE RESPONSE =====
        result.put("matchScore", score);
        result.put("resumeSkills", resumeSkills);
        result.put("missingSkills", missingSkills);
        result.put("recommendation", message);

    } catch (Exception e) {

        e.printStackTrace();

        result.put("error", e.getMessage());
    }

    return result;
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