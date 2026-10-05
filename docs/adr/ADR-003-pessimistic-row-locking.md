# ADR 003: Pessimistic Row-Level Locking for Resource Reservations

## Context
When processing a reservation, multiple threads attempting to book slots for the same resource simultaneously can cause race conditions during application-level overlap verification.

## Decision
We implemented pessimistic write locking (`@Lock(LockModeType.PESSIMISTIC_WRITE)`) when loading the target `Resource` during booking creation:
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT r FROM Resource r LEFT JOIN FETCH r.openingHours WHERE r.id = :id")
Optional<Resource> findByIdForUpdate(@Param("id") Long id);
```

## Consequences
- **Positive:** Serializes booking attempts per resource, allowing application-level overlap checks to deterministically catch conflicts and return HTTP 409 Conflict without relying solely on DB constraint exceptions.
- **Negative:** Holds a database row lock for the duration of the transaction.
