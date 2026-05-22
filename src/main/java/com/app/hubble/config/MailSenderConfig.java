package com.app.hubble.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import java.util.Properties;

@Configuration
public class MailSenderConfig {
    @Bean
    public JavaMailSender javaMailSender(
            @Value("${spring.mail.host}") String host,
            @Value("${spring.mail.port}") int port,
            @Value("${spring.mail.protocol}") String protocol,
            @Value("${spring.mail.username}") String username,
            @Value("${spring.mail.password}") String password,
            @Value("${spring.mail.default-encoding}") String defaultEncoding,
            @Value("${spring.mail.properties.mail.smtp.auth}") String smtpAuth,
            @Value("${spring.mail.properties.mail.smtp.starttls.enable}") String startTlsEnable,
            @Value("${spring.mail.properties.mail.smtp.starttls.required}") String startTlsRequired,
            @Value("${spring.mail.properties.mail.smtp.ssl.enable}") String sslEnable,
            @Value("${spring.mail.properties.mail.smtp.connectiontimeout}") String connectionTimeout,
            @Value("${spring.mail.properties.mail.smtp.timeout}") String timeout,
            @Value("${spring.mail.properties.mail.smtp.writetimeout}") String writeTimeout
    ) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setProtocol(protocol);
        mailSender.setUsername(username);
        mailSender.setPassword(password.replace(" ", ""));
        mailSender.setDefaultEncoding(defaultEncoding);
        Properties javaMailProperties = new Properties();
        javaMailProperties.put("mail.smtp.auth", smtpAuth);
        javaMailProperties.put("mail.smtp.starttls.enable", startTlsEnable);
        javaMailProperties.put("mail.smtp.starttls.required", startTlsRequired);
        javaMailProperties.put("mail.smtp.ssl.enable", sslEnable);
        javaMailProperties.put("mail.smtp.connectiontimeout", connectionTimeout);
        javaMailProperties.put("mail.smtp.timeout", timeout);
        javaMailProperties.put("mail.smtp.writetimeout", writeTimeout);
        mailSender.setJavaMailProperties(javaMailProperties);
        return mailSender;
    }
}
