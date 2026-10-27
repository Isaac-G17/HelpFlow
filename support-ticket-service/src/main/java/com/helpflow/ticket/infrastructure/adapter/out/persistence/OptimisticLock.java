package com.helpflow.ticket.infrastructure.adapter.out.persistence;

import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.Objects;

/**
 * Compara la versión con la que el dominio leyó el objeto contra la versión actual de la entidad.
 * Si alguien la modificó entre medias, falla con 409 en vez de pisar sus cambios.
 * (Dentro de la misma transacción, Hibernate además verifica la versión en el UPDATE ... WHERE version = ?).
 */
final class OptimisticLock {

    private OptimisticLock() {
    }

    static void check(Class<?> type, Object id, Long domainVersion, Long entityVersion) {
        if (domainVersion != null && !Objects.equals(domainVersion, entityVersion)) {
            throw new ObjectOptimisticLockingFailureException(type, id);
        }
    }
}
