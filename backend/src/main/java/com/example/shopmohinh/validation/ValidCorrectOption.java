package com.example.shopmohinh.validation;

import com.example.shopmohinh.validation.ValidCorrectOptionValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = ValidCorrectOptionValidator.class)
@Target({ TYPE })
@Retention(RUNTIME)
public @interface ValidCorrectOption {
    String message() default "correctOption must be a valid index for options";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
