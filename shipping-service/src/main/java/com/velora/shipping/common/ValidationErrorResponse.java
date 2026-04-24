package com.velora.shipping.common;

import java.util.List;

public class ValidationErrorResponse {
    private String status;
    private int code;
    private String message;
    private List<FieldErrorDetail> errors;

    public ValidationErrorResponse() {}
    public ValidationErrorResponse(String status, int code, String message, List<FieldErrorDetail> errors) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.errors = errors;
    }

    public static class FieldErrorDetail {
        private String field;
        private String message;
        public FieldErrorDetail() {}
        public FieldErrorDetail(String field, String message) {
            this.field = field;
            this.message = message;
        }
        public String getField() { return field; }
        public void setField(String field) { this.field = field; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public List<FieldErrorDetail> getErrors() { return errors; }
    public void setErrors(List<FieldErrorDetail> errors) { this.errors = errors; }
}
