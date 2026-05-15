package com.app.hubble.mapper;

import com.app.hubble.dto.request.PaymentRequest;
import com.app.hubble.dto.response.PaymentResponse;
import com.app.hubble.entity.Payment;
import com.app.hubble.enumeration.PaymentStatus;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class PaymentMapper {
    public Payment toEntity(PaymentRequest request) {
        return Payment.builder()
                .id(request.getId())
                .patientId(request.getPatientId())
                .appointmentId(request.getAppointmentId())
                .examId(request.getExamId())
                .prescriptionId(request.getPrescriptionId())
                .paymentCardId(request.getPaymentCardId())
                .amount(request.getAmount() != null ? request.getAmount() : BigDecimal.ZERO)
                .method(request.getMethod())
                .status(request.getStatus() != null ? request.getStatus() : PaymentStatus.PENDING)
                .active(request.isActive())
                .build();
    }

    public PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .patientId(payment.getPatientId())
                .appointmentId(payment.getAppointmentId())
                .examId(payment.getExamId())
                .prescriptionId(payment.getPrescriptionId())
                .paymentCardId(payment.getPaymentCardId())
                .amount(payment.getAmount())
                .method(payment.getMethod())
                .status(payment.getStatus())
                .active(payment.isActive())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}
