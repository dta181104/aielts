package com.example.shopmohinh.validation;

import com.example.shopmohinh.dto.request.QuestionRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class ValidCorrectOptionValidator implements ConstraintValidator<ValidCorrectOption, QuestionRequest> {

    @Override
    public boolean isValid(QuestionRequest req, ConstraintValidatorContext context) {
        if (req == null) return true;
        List<String> options = req.getOptions();
        Integer idx = req.getCorrectOption();

        if (options == null || options.isEmpty()) {
            // Let @Size handle the options validation; if options invalid, skip index check
            return idx != null;
        }
        if (idx == null) return false;
        return idx >= 0 && idx < options.size();
    }
}
