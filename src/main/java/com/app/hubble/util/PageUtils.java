package com.app.hubble.util;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.List;

public class PageUtils {
    public static <T> Mono<PageResponse<T>> buildPage(
            Flux<T> data,
            Mono<Long> total,
            int page,
            int size
    ) {
        return Mono.zip(data.collectList(), total)
                .map(tuple -> {
                    List<T> content = tuple.getT1();
                    long totalElements = tuple.getT2();

                    int totalPages = (int) Math.ceil((double) totalElements / size);

                    return new PageResponse<>(
                            content,
                            page,
                            size,
                            totalElements,
                            totalPages
                    );
                });
    }
}
