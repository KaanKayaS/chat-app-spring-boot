package com.app.chat_app.core.dto;

import java.util.List;

import org.springframework.data.domain.Page;

/**
 * Tüm sayfalı endpoint'lerin ortak response formatı.
 *
 * items        → mevcut sayfadaki elemanlar
 * page         → mevcut sayfa numarası (0-based)
 * size         → sayfa başına eleman sayısı
 * totalItems   → toplam eleman sayısı (DB'deki)
 * totalPages   → toplam sayfa sayısı
 * hasNext      → sonraki sayfa var mı (infinite scroll için)
 */
public record PagedResponse<T>(
        List<T> items,
        int page,
        int size,
        long totalItems,
        int totalPages,
        boolean hasNext
) {
    public static <T> PagedResponse<T> from(Page<T> page) {
        return new PagedResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
    }

    public static <T> PagedResponse<T> of(List<T> items, int page, int size, long totalItems) {
        int totalPages = size == 0 ? 1 : (int) Math.ceil((double) totalItems / size);
        boolean hasNext = (long) (page + 1) * size < totalItems;
        return new PagedResponse<>(items, page, size, totalItems, totalPages, hasNext);
    }
}
