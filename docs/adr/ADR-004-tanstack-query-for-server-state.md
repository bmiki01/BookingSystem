# ADR 004: TanStack Query for Frontend Server State Management

## Context
The React frontend needs to fetch, cache, and synchronize server data (resource lists, availability grids, booking statuses) while maintaining high UI responsiveness.

## Decision
We chose TanStack Query (React Query v5) for server state management instead of manual `useEffect` or Redux.

## Consequences
- **Positive:** Automatic caching, background refetching, query key invalidation upon booking creation/cancellation, and built-in loading/error states.
- **Negative:** Adds a client-side dependency (~12kB gzipped).
