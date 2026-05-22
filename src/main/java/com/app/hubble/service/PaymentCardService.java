package com.app.hubble.service;

import com.app.hubble.dto.request.PaymentCardRequest;
import com.app.hubble.dto.response.PaymentCardResponse;
import com.app.hubble.entity.PaymentCard;
import com.app.hubble.entity.User;
import com.app.hubble.enumeration.UserRole;
import com.app.hubble.exception.BadRequestException;
import com.app.hubble.exception.NotFoundException;
import com.app.hubble.exception.UnauthorizedException;
import com.app.hubble.repository.PaymentCardRepository;
import com.app.hubble.repository.UserRepository;
import com.app.hubble.util.PageResponse;
import com.app.hubble.util.PageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentCardService {
    private final PaymentCardRepository paymentCardRepository;
    private final UserRepository userRepository;

    public Mono<PageResponse<PaymentCardResponse>> findAllPaymentCards(int page, int size) {
        int offset = page * size;
        Flux<PaymentCardResponse> data = paymentCardRepository.findAllActivePaged(size, offset).map(this::toResponse);
        Mono<Long> total = paymentCardRepository.countByDeletedAtIsNull();
        return PageUtils.buildPage(data, total, page, size);
    }

    public Mono<PageResponse<PaymentCardResponse>> findPaymentCardsForCurrentUser(UUID userId, int page, int size) {
        return userRepository.findById(userId)
                .filter(User::isActive)
                .filter(u -> u.getDeletedAt() == null)
                .switchIfEmpty(Mono.error(new UnauthorizedException("Usuario no encontrado.")))
                .flatMap(user -> {
                    if (user.getRole() == UserRole.ADMIN) {
                        return findAllPaymentCards(page, size);
                    }
                    return pageCardsForUser(userId, page, size);
                });
    }

    private Mono<PageResponse<PaymentCardResponse>> pageCardsForUser(UUID userId, int page, int size) {
        int offset = page * size;
        Flux<PaymentCardResponse> data = paymentCardRepository.findPagedByUserId(userId, size, offset)
                .map(this::toResponse);
        Mono<Long> total = paymentCardRepository.countByUserId(userId);
        return PageUtils.buildPage(data, total, page, size);
    }

    public Mono<PaymentCardResponse> findPaymentCardById(UUID id) {
        return paymentCardRepository.findById(id)
                .filter(c -> c.getDeletedAt() == null)
                .switchIfEmpty(Mono.error(new NotFoundException("Tarjeta no encontrada.")))
                .map(this::toResponse);
    }

    public Mono<PaymentCardResponse> savePaymentCard(PaymentCardRequest body) {
        LocalDateTime now = LocalDateTime.now();
        short month = body.getExpMonth().shortValue();
        short year = body.getExpYear().shortValue();
        return userRepository.findById(body.getUserId())
                .filter(u -> u.getDeletedAt() == null)
                .switchIfEmpty(Mono.error(new NotFoundException("Usuario no encontrado.")))
                .flatMap(u -> {
                    PaymentCard row = PaymentCard.builder()
                            .userId(body.getUserId())
                            .provider(body.getProvider().trim())
                            .providerCardToken(body.getProviderCardToken().trim())
                            .brand(body.getBrand().trim())
                            .lastFour(body.getLastFour().trim())
                            .expMonth(month)
                            .expYear(year)
                            .holderName(body.getHolderName().trim())
                            .defaultForPatient(body.isDefaultCard())
                            .active(true)
                            .createdAt(now)
                            .updatedAt(now)
                            .build();
                    return paymentCardRepository.save(row);
                })
                .map(this::toResponse);
    }

    public Mono<PaymentCardResponse> updatePaymentCard(PaymentCardRequest body) {
        if (body.getId() == null) {
            return Mono.error(new BadRequestException("Id de tarjeta es nulo."));
        }
        LocalDateTime now = LocalDateTime.now();
        short month = body.getExpMonth().shortValue();
        short year = body.getExpYear().shortValue();
        return paymentCardRepository.findById(body.getId())
                .filter(c -> c.getDeletedAt() == null)
                .switchIfEmpty(Mono.error(new NotFoundException("Tarjeta no encontrada.")))
                .flatMap(row -> userRepository.findById(body.getUserId())
                        .filter(u -> u.getDeletedAt() == null)
                        .switchIfEmpty(Mono.error(new NotFoundException("Usuario no encontrado.")))
                        .flatMap(u -> {
                            row.setUserId(body.getUserId());
                            row.setProvider(body.getProvider().trim());
                            row.setProviderCardToken(body.getProviderCardToken().trim());
                            row.setBrand(body.getBrand().trim());
                            row.setLastFour(body.getLastFour().trim());
                            row.setExpMonth(month);
                            row.setExpYear(year);
                            row.setHolderName(body.getHolderName().trim());
                            row.setDefaultForPatient(body.isDefaultCard());
                            row.setActive(body.isActive());
                            row.setUpdatedAt(now);
                            return paymentCardRepository.save(row);
                        }))
                .map(this::toResponse);
    }

    public Mono<Void> deletePaymentCard(UUID id) {
        LocalDateTime now = LocalDateTime.now();
        return paymentCardRepository.findById(id)
                .filter(c -> c.getDeletedAt() == null)
                .switchIfEmpty(Mono.error(new NotFoundException("Tarjeta no encontrada.")))
                .flatMap(row -> {
                    row.setDeletedAt(now);
                    row.setUpdatedAt(now);
                    row.setActive(false);
                    return paymentCardRepository.save(row);
                })
                .then();
    }

    private PaymentCardResponse toResponse(PaymentCard c) {
        return PaymentCardResponse.builder()
                .id(c.getId())
                .userId(c.getUserId())
                .provider(c.getProvider())
                .brand(c.getBrand())
                .lastFour(c.getLastFour())
                .expMonth(c.getExpMonth())
                .expYear(c.getExpYear())
                .holderName(c.getHolderName())
                .defaultCard(c.isDefaultForPatient())
                .active(c.isActive())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
