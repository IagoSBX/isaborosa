package com.isaborosa.biblioteca.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record PriceDto(String store, BigDecimal price, String url, Instant checkedAt) {
}
