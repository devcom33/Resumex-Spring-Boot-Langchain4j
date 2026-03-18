package org.heymouad.luminadocs.controller;


import org.heymouad.luminadocs.domain.MatchResult;
import org.heymouad.luminadocs.domain.ResumeResult;
import org.heymouad.luminadocs.service.ResumeProcessor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


@RequestMapping("/api/v1/resume")
@RestController
public class AnalysisController {

    private final ResumeProcessor resumeProcessor;

    public AnalysisController(ResumeProcessor resumeProcessor)
    {
        this.resumeProcessor = resumeProcessor;
    }

    @PostMapping("/upload")
    public ResumeResult upload(@RequestParam("file")MultipartFile file) throws IOException {
        return resumeProcessor.analyze(file.getInputStream());
    }

    @PostMapping("/match")
    public MatchResult matchWithJob(
            @RequestParam("file") MultipartFile file,
            @RequestParam("jd") String jobDescription) throws IOException {
        return resumeProcessor.calculateMatch(file.getInputStream(), jobDescription);
    }

    @PostMapping("/generate-pdf")
    public ResponseEntity<byte[]> generateTailoredPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam("jd") String jobDescription) throws IOException, InterruptedException {

        ResumeResult result = resumeProcessor.tailorResume(file.getInputStream(), jobDescription);
        String generatedFileName = resumeProcessor.convertLatexToPdf(result.latexCode());
        Path pdfPath = Path.of(generatedFileName);

        try {
            if (!Files.exists(pdfPath)) {
                return ResponseEntity.internalServerError().build();
            }

            byte[] contents = Files.readAllBytes(pdfPath);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"tailored_resume.pdf\"")
                    .body(contents);
        } finally {
            Files.deleteIfExists(pdfPath);
        }
    }
}
