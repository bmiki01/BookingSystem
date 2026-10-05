# ADR 001: PostgreSQL Exclusion Constraints for Double-Booking Prevention

## Context
In a booking system, two concurrent transactions might attempt to reserve the exact same resource for overlapping time slots at the exact same millisecond. Relying solely on application-level checks in `READ_COMMITTED` transaction isolation can fail under concurrent load.

## Decision
We decided to enforce database-level exclusion constraints using PostgreSQL's `btree_gist` extension:
```sql
ALTER TABLE bookings
ADD CONSTRAINT no_overlapping_bookings
EXCLUDE USING gist (
    resource_id WITH =,
    tstzrange(start_time, end_time, '[)') WITH &&
) WHERE (status = 'CONFIRMED');
```

## Consequences
- **Positive:** Guarantees 100% prevention of double bookings at the database engine level, even across multi-node application clusters.
- **Negative:** Constraint is specific to PostgreSQL. Requires catching `DataIntegrityViolationException` in Spring Boot and mapping it to `HTTP 409 Conflict`.
