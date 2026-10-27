package com.helpflow.ticket.domain.model;

/** Paginación independiente de Spring (el dominio no conoce Pageable). */
public record PageQuery(int page, int size) {

    public PageQuery {
        if (page < 0) {
            page = 0;
        }
        if (size < 1 || size > 100) {
            size = 20;
        }
    }
}
