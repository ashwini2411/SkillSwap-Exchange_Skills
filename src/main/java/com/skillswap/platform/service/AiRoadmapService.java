package com.skillswap.platform.service;

import org.springframework.stereotype.Service;

@Service
public class AiRoadmapService {

    public String generateRoadmap(String requestedSkill, String offeredSkill) {
        // Simulated AI response
        return "<h3>AI-Generated Roadmap: " + requestedSkill + " &#8644; " + offeredSkill + "</h3>" +
               "<div style='margin-top:15px; text-align: left;'>" +
               "<h4>Week 1: Fundamentals</h4>" +
               "<ul><li>Understand the core concepts of " + requestedSkill + ".</li>" +
               "<li>Set up the environment and build a hello-world project.</li></ul>" +
               "<h4>Week 2: Deep Dive</h4>" +
               "<ul><li>Explore advanced syntax and common libraries.</li>" +
               "<li>Partner code-review session on " + offeredSkill + ".</li></ul>" +
               "<h4>Week 3: Practice & Building</h4>" +
               "<ul><li>Create a mini-project combining both skills if possible.</li>" +
               "<li>Debug common errors and learn best practices.</li></ul>" +
               "<h4>Week 4: Mastery & Capstone</h4>" +
               "<ul><li>Finalize the mini-project.</li>" +
               "<li>Press 'Complete Swap' to validate skills and earn your badge!</li></ul>" +
               "</div>";
    }
}
