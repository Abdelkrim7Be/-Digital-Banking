package com.bellagnech.dig_bank.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;

/**
 * Response body for validation errors
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidationErrorResponse {
    private Date timestamp;
    private String message;
    private Map<String, String> errors;
}
