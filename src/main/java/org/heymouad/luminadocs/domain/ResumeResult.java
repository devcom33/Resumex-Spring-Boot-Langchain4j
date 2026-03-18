package org.heymouad.luminadocs.domain;


import java.util.List;
import java.util.Map;

public record ResumeResult (
        String fullName,
        String email,
        String phoneNumber,
        String professionalSummary,
        Map<String, List<String>> categorizedSkills,
        List<WorkHistory> workHistory,
        String latexCode
){
}

record WorkHistory(String company, String role, String duration) {}