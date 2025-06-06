package com.example.web_qltv_be.service.email;

public interface EmailService {
    public void sendMessage(String from, String to, String subject, String message);
}
