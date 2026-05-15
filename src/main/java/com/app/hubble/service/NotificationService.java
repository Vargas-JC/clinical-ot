package com.app.hubble.service;

import com.app.hubble.brand.ClinicBranding;
import com.app.hubble.entity.Notification;
import com.app.hubble.entity.User;
import com.app.hubble.enumeration.NotificationType;
import com.app.hubble.repository.NotificationRepository;
import com.app.hubble.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SpringTemplateEngine mailTemplateEngine;
    private final JavaMailSender javaMailSender;
    private final Environment environment;

    public Mono<Void> onUserRegistered(User user) {
        String title = "Cuenta creada en " + ClinicBranding.DISPLAY_NAME;
        String body = "Hola " + user.getFullName() + ". Tu cuenta ya está activa.";
        return persistAndEmail(user.getId(), null, null, NotificationType.USER_ACCOUNT_CREATED, title, body, user.getEmail());
    }

    public Mono<Void> onAppointmentReserved(UUID appointmentId, UUID patientUserId, String patientName, LocalDateTime when) {
        return userRepository.findById(patientUserId)
                .flatMap(user -> {
                    String title = "Cita reservada";
                    String body = "Hola " + patientName + ". Tienes una cita el " + FMT.format(when) + ".";
                    return persistAndEmail(user.getId(), appointmentId, null, NotificationType.APPOINTMENT_RESERVED, title, body, user.getEmail());
                });
    }

    public Mono<Void> onPaymentCompleted(UUID paymentId, UUID patientUserId, String amountLabel) {
        return userRepository.findById(patientUserId)
                .flatMap(user -> {
                    String title = "Pago registrado";
                    String body = "Se registró un pago por " + amountLabel + ".";
                    return persistAndEmail(user.getId(), null, paymentId, NotificationType.PAYMENT_COMPLETED, title, body, user.getEmail());
                });
    }

    public Mono<Void> sendPasswordResetOtpEmail(String emailTo, String userName, String otpCode, int validityMinutes) {
        return Mono.fromRunnable(() -> {
            try {
                String title = "Recuperación de contraseña — " + ClinicBranding.DISPLAY_NAME;
                Context ctx = new Context();
                ctx.setVariable("title", title);
                ctx.setVariable("userName", userName);
                ctx.setVariable("otpCode", otpCode);
                ctx.setVariable("validityMinutes", validityMinutes);
                String html = mailTemplateEngine.process("password-reset", ctx);
                MimeMessage mimeMessage = javaMailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                helper.setFrom(ClinicBranding.DISPLAY_NAME + " <" + mailUsername() + ">");
                helper.setTo(emailTo);
                helper.setSubject(title);
                helper.setText(html, true);
                ClassPathResource logo = new ClassPathResource("logo/logo-negro.png");
                if (logo.exists()) {
                    helper.addInline("logo", logo);
                }
                javaMailSender.send(mimeMessage);
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    private String mailUsername() {
        String u = environment.getProperty("spring.mail.username");
        return u == null ? "" : u;
    }

    private Mono<Void> persistAndEmail(
            UUID userId,
            UUID appointmentId,
            UUID paymentId,
            NotificationType type,
            String title,
            String body,
            String emailTo
    ) {
        LocalDateTime now = LocalDateTime.now();
        Notification row = Notification.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .body(body)
                .appointmentId(appointmentId)
                .paymentId(paymentId)
                .active(true)
                .createdAt(now)
                .updatedAt(now)
                .build();
        return notificationRepository.save(row)
                .flatMap(saved -> sendSimpleNotificationEmail(emailTo, title, body)
                        .then(Mono.defer(() -> notificationRepository.save(
                                saved.toBuilder().emailSentAt(LocalDateTime.now()).build()
                        )))
                        .onErrorResume(e -> Mono.empty())
                )
                .then();
    }

    private Mono<Void> sendSimpleNotificationEmail(String to, String title, String message) {
        return Mono.fromRunnable(() -> {
            try {
                Context ctx = new Context();
                ctx.setVariable("title", title);
                ctx.setVariable("message", message);
                String html = mailTemplateEngine.process("notification", ctx);
                MimeMessage mimeMessage = javaMailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                helper.setFrom(ClinicBranding.DISPLAY_NAME + " <" + mailUsername() + ">");
                helper.setTo(to);
                helper.setSubject(title);
                helper.setText(html, true);
                ClassPathResource logo = new ClassPathResource("logo/logo-negro.png");
                if (logo.exists()) {
                    helper.addInline("logo", logo);
                }
                javaMailSender.send(mimeMessage);
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }
}
