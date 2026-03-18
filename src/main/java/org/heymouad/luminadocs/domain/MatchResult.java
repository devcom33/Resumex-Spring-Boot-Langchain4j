package org.heymouad.luminadocs.domain;

import java.util.List;

public record MatchResult(
        int matchPercentage,
        List<String> matchingSkills,
        List<String> missingSkills,
        String verdict,
        String suggestion
) {
}
