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
        Your task is to generate a professional resume using the provided template style.

        CRITICAL ESCAPING RULES:
        1. Every single '&' MUST be written as '\\&'.
        2. Every single '_' MUST be written as '\\_'.
        3. Every single '%' MUST be written as '\\%'.

        STRUCTURE INSTRUCTIONS:
        1. Return ONLY the raw LaTeX code. No markdown code blocks (```), no chat.
        2. Use the exact commands provided in the BASE STRUCTURE below.

        LATEX BASE STRUCTURE:
        {{latexPreamble}}
        """)
    @UserMessage("""
        Tailor this resume to the Job Description (JD). 
        
        MAPPING RULES:
        - Jobs: \\resumeSubheading{Company}{Location}{Role}{Date}
        - Bullets: \\resumeItem{Text}
        - Projects: \\resumeProjectHeading{\\textbf{Name} $|$ \\emph{Stack}}{Date}

        Resume Data: {{resumeText}} 
        Job Description: {{jd}}
        """)
    String generateLatex(
            @V("latexPreamble") String latexPreamble,
            @V("resumeText") String resumeText,
            @V("jd") String jd
    );

}
