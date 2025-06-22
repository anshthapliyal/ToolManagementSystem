package com.coditas.tool.management.system.service;

public interface EmailService {

    public void sendEmail(String to, String subject, String body);
}
