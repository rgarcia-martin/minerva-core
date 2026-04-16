---
name: Minerva Core project scope and current state
description: What Minerva Core is, the bounded contexts that exist today, and which user stories from requisitos.txt are still pending
type: project
---
Minerva Core is the back-end of a small-business **point-of-sale / retail management system** (Spring Boot REST API, DDD, layered per aggregate). The user is building it incrementally through TDD/DDD iterations and uses `api-tests.sh` as the executable acceptance suite.

**Bounded contexts implemented (full CRUD unless noted)**: catalog (article, tax, freeconcept), inventory (item — read-only via REST, created by purchases; location), identity (user with PBKDF2 password hashing), purchasing (provider, purchase), payment (paymentmethod with CASH/CARD/GATEWAY), sales (sale; client exists as a domain record only — no service/controller yet), shared.

**Notable domain mechanics already in place**:
- Purchase auto-generates inventory items per unit, including a parent/child split when an article `canHaveChildren` and the line is purchased in `OPENED` state (the "box of pens" flow).
- Sale validates references, marks items as `SOLD`, and on delete releases them back to `AVAILABLE`.
- SaleLine is XOR over `itemId` / `freeConceptId`; item lines must have quantity == 1.

**Pending from requisitos.txt** (not yet implemented as of 2026-04-11): role-based authorization enforcement, login/authentication flow, deposit purchase returns at expiry, automatic surcharge-of-equivalence calculation per line when the provider applies it, automatic stock allocation by margin (simplex/AI/heuristics), client CRUD endpoints, ticket/invoice generation.

**Why:** Future sessions need to know which features are "missing on purpose" (pending in the backlog) vs "missing as a bug" before suggesting changes or marking work as complete.

**How to apply:** Before proposing new functionality, check `requisitos.txt` to see if the user has already framed it. Before claiming a feature is complete, sanity-check the corresponding section in `api-tests.sh`. Treat the absence of a Client controller as intentional, not a gap to fix unprompted.
