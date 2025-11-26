package com.example.shopmohinh.validation;

import com.example.shopmohinh.dto.request.QuestionRequest;
import com.example.shopmohinh.util.QuestionOptionUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class ValidCorrectOptionValidator implements ConstraintValidator<ValidCorrectOption, QuestionRequest> {

    @Override
    public boolean isValid(QuestionRequest req, ConstraintValidatorContext context) {
        if (req == null) return true;
        List<String> options = req.getOptions();
        String raw = req.getCorrectOption();

        if (options == null || options.isEmpty()) {
            // If there are no options, correctOption should be null or empty (for open answer)
            return raw == null || raw.trim().isEmpty();
        }
        if (raw == null || raw.trim().isEmpty()) return false;

        // Try to parse: allow letter (A/B/...) or numeric index (0-based)
        String normalized = QuestionOptionUtils.toLetter(raw);
        if (normalized == null) return false;

        int idx = normalized.charAt(0) - 'A';
        return idx >= 0 && idx < options.size();
    }
}
