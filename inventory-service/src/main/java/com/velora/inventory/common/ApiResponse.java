package com.velora.inventory.common;

public class ApiResponse<T> {
    private String status;
    private int code;
    private String message;
    private T data;

    public ApiResponse() {}

    public ApiResponse(String status, int code, String message, T data) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(int code, String message, T data) {
        return new ApiResponse<>("success", code, message, data);
    }

    public static <T> ApiResponse<T> error(int code, String message, T data) {
        return new ApiResponse<>("error", code, message, data);
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}
