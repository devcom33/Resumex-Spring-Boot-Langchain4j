package org.heymouad.luminadocs.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;
import org.heymouad.luminadocs.domain.MatchResult;
import org.heymouad.luminadocs.domain.ResumeResult;


@AiService
public interface ResumeSpecialist {

    @SystemMessage("""
    You are a Technical Recruiter. Parse the resume and group skills into exactly these categories:
    - "Backend": Languages and frameworks like Java, Spring, Python.
    - "Frontend": React, Angular, CSS frameworks.
    - "DevOps & Tools": Docker, CI/CD, Git, Linux.
    - "AI & Data": Machine Learning, SQL, Redis, LLMs.
    
    If a category is empty, omit it.
    """)
    ResumeResult parse(@UserMessage String text);

    @SystemMessage("""
        You are a hiring manager.
        Compare the provided Resume against the Job Description.
        1. Calculate a match percentage.
        2. Identify matching skills.
        3. Identify missing skills.
        4. Provide a verdict and advice.
        """)
    @UserMessage("""
        Here is the resume content:
        {{resumeText}}
        
        And here is the job description:
        {{jobDescription}}
        """)
    MatchResult checkMatch(@V("resumeText") String resumeText, @V("jobDescription") String jobDescription);

    @SystemMessage("""
        You are a LaTeX Resume Expert.
        Use the following LaTeX structure as your base:
        
        \\documentclass{article}
        \\begin{document}
        \\section{Name} {{fullName}}
        \\section{Summary} {{tailoredSummary}}
        \\section{Skills} {{skills}}
        \\end{document}
        
        Replace the placeholders with content tailored to the Job Description. 
        Ensure the LaTeX is valid and compilable.
        """)
    @UserMessage("Generate LaTeX for Resume: {{resumeText}} based on JD: {{jd}}")
    String generateLatex(@V("resumeText") String resumeText, @V("jd") String jd);
}
