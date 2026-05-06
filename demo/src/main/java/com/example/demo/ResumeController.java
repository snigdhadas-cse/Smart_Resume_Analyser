package com.example.demo;

import java.util.*;
import java.nio.file.*;
import org.apache.tika.Tika;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin(origins = "*")
@RestController
public class ResumeController {

    private List<String> skillsList = List.of(
    "java", "python", "sql", "spring", "html", "css",
    "javascript", "react", "node", "mongodb"
);

    private List<String> extractSkills(String text) {
    List<String> foundSkills = new ArrayList<>();

    String lowerText = text.toLowerCase();

    for (String skill : skillsList) {
        if (lowerText.contains(skill)) {
            foundSkills.add(skill);
        }
    }

    return foundSkills;
}

    @PostMapping("/match")
public Map<String, Object> matchResume(
        @RequestParam("file") MultipartFile file,
        @RequestParam("jobDescription") String jobDesc) {

    Map<String, Object> result = new HashMap<>();

    try {
        // Step 1: Extract resume text
        Tika tika = new Tika();
        String resumeText = tika.parseToString(file.getInputStream());

        // Step 2: Extract skills (this uses your Step 2 method)
        List<String> resumeSkills = extractSkills(resumeText);
        List<String> jobSkills = extractSkills(jobDesc);

        // Step 3: Match calculation
        int matchCount = 0;
        for (String skill : jobSkills) {
            if (resumeSkills.contains(skill)) {
                matchCount++;
            }
        }

        double score = 0;
if (jobSkills.size() > 0) {
    score = (double) matchCount / jobSkills.size() * 100;
}

// round score
score = Math.round(score * 100.0) / 100.0;

String message;

if (score >= 75) {
    message = "Strong match";
} else if (score >= 50) {
    message = "Moderate match";
} else {
    message = "Needs improvement";
}

        // Step 4: Missing skills
        List<String> missing = new ArrayList<>();
        for (String skill : jobSkills) {
            if (!resumeSkills.contains(skill)) {
                missing.add(skill);
            }
        }

        // Step 5: Return result
        result.put("matchScore", score);
        result.put("resumeSkills", resumeSkills);
        result.put("missingSkills", missing);
        result.put("recommendation", message);

    } catch (Exception e) {
        result.put("error", "Processing failed");
    }

    return result;
}



@PostMapping("/upload")
public String uploadFile(@RequestParam("file") MultipartFile file) {
    try {
        // create uploads folder if not exists
        Path uploadPath = Paths.get("uploads");
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // save file
        Path filePath = uploadPath.resolve(file.getOriginalFilename());
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return "File saved: " + file.getOriginalFilename();

    } catch (Exception e) {
        return "Error saving file";
    }
}
}