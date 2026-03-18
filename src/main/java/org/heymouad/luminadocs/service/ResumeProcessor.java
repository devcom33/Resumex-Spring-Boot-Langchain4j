package org.heymouad.luminadocs.service;


import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import org.heymouad.luminadocs.domain.MatchResult;
import org.heymouad.luminadocs.domain.ResumeResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;


@Service
public class ResumeProcessor {
    @Value("classpath:latex-preamble.txt")
    private Resource latexPreambleResource;

    private final ResumeSpecialist specialist;

    public ResumeProcessor(ResumeSpecialist specialist) {
        this.specialist = specialist;
    }

    public ResumeResult analyze(InputStream inputStream) {
        ApachePdfBoxDocumentParser parser = new ApachePdfBoxDocumentParser();
        Document document = parser.parse(inputStream);
        return specialist.parse(document.text());
    }

    public MatchResult calculateMatch(InputStream pdfStream, String jd) {
        ApachePdfBoxDocumentParser parser = new ApachePdfBoxDocumentParser();
        String resumeText = parser.parse(pdfStream).text();
        return specialist.checkMatch(resumeText, jd);
    }

    public ResumeResult tailorResume(InputStream pdfStream, String jd) {
        try {
            ApachePdfBoxDocumentParser parser = new ApachePdfBoxDocumentParser();
            String resumeText = parser.parse(pdfStream).text();

            // Read the preamble from the injected Resource
            String preamble = latexPreambleResource.getContentAsString(StandardCharsets.UTF_8);

            // Get structured data for the response object
            ResumeResult result = specialist.parse(resumeText);


            String tailoredLatex = specialist.generateLatex(preamble, resumeText, jd);

            return new ResumeResult(
                    result.fullName(), result.email(), result.phoneNumber(),
                    result.professionalSummary(), result.categorizedSkills(),
                    result.workHistory(), tailoredLatex
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to read LaTeX preamble from resources", e);
        }
    }

    public String convertLatexToPdf(String latexContent) throws IOException, InterruptedException {
        String baseName = "resume_" + System.currentTimeMillis();
        Path texPath = Path.of(baseName + ".tex");
        Path pdfPath = Path.of(baseName + ".pdf");

        // Add Sanitization
        String safeLatex = latexContent.replace(" & ", " \\& ")
                .replace("_", "\\_");
        Files.writeString(texPath, safeLatex);

        // Run pdflatex
        ProcessBuilder pb = new ProcessBuilder("pdflatex", "-interaction=nonstopmode", texPath.toString());
        Process process = pb.start();
        boolean finished = process.waitFor(30, TimeUnit.SECONDS);

        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException("LaTeX timeout");
        }

        // Check if the PDF was actually created, regardless of exit code
        if (Files.exists(pdfPath)) {
            Files.deleteIfExists(texPath);
            Files.deleteIfExists(Path.of(baseName + ".log"));
            Files.deleteIfExists(Path.of(baseName + ".aux"));
            Files.deleteIfExists(Path.of(baseName + ".out"));

            return pdfPath.getFileName().toString();
        } else {
            throw new RuntimeException("pdflatex failed to generate PDF. Exit code: " + process.exitValue());
        }
    }

    private void cleanup(String baseName, Path texPath) throws IOException {
        Files.deleteIfExists(texPath);
        Files.deleteIfExists(Path.of(baseName + ".log"));
        Files.deleteIfExists(Path.of(baseName + ".aux"));
    }
}