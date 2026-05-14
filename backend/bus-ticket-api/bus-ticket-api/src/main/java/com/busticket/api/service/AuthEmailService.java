package com.busticket.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class AuthEmailService {

    private final JavaMailSender mailSender;
    private final String mailFrom;
    private final String frontendUrl;

    public AuthEmailService(
            JavaMailSender mailSender,
            @Value("${spring.mail.username:}") String mailFrom,
            @Value("${app.frontend-url}") String frontendUrl
    ) {
        this.mailSender = mailSender;
        this.mailFrom = mailFrom;
        this.frontendUrl = trimTrailingSlash(frontendUrl);
    }

    public void sendVerificationEmail(String to, String token) {
        String link = UriComponentsBuilder
                .fromUriString(frontendUrl + "/verify-email.html")
                .queryParam("token", token)
                .build()
                .toUriString();

        send(to, "Xac thuc tai khoan Bus Ticket",
                "Chao ban,\n\n"
                        + "Vui long bam vao link ben duoi de xac thuc tai khoan:\n"
                        + link + "\n\n"
                        + "Link co hieu luc trong 60 giay.\n"
                        + "Neu ban khong tao tai khoan, vui long bo qua email nay.");
    }

    public void sendPasswordResetEmail(String to, String token) {
        String link = UriComponentsBuilder
                .fromUriString(frontendUrl + "/reset-password.html")
                .queryParam("token", token)
                .build()
                .toUriString();

        send(to, "Dat lai mat khau Bus Ticket",
                "Chao ban,\n\n"
                        + "Vui long bam vao link ben duoi de dat lai mat khau:\n"
                        + link + "\n\n"
                        + "Link co hieu luc trong 90 giay.\n"
                        + "Neu ban khong yeu cau dat lai mat khau, vui long bo qua email nay.");
    }

    private void send(String to, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        mailSender.send(message);
    }

    private String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
