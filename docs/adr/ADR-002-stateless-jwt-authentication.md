# ADR 002: Stateless JWT Authentication

## Context
The application requires secure authentication for both web frontends and potential mobile/third-party API consumers.

## Decision
We chose stateless JWT (JSON Web Token) authentication using Spring Security and the `jjwt` library. Tokens carry user identity and role claims and are verified statelessly on each request.

## Consequences
- **Positive:** No server-side session state stored in memory or Redis, enabling effortless horizontal scaling.
- **Negative:** Token revocation before expiration requires token blacklisting if needed in the future.
