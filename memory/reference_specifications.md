---
name: Specification and acceptance-test sources
description: Where to find the authoritative product spec (Spanish user stories) and the end-to-end functional acceptance tests
type: reference
---
When you need to understand WHAT the system is supposed to do (not how it is implemented), check these two artifacts at the project root before grepping the code:

- `requisitos.txt` — Spanish user stories grouped by aggregate (`#usuarios`, `#localizaciones`, `#impuestos`, `#articulos`, `#proveedores`, `#albaranes de compra`, `#métodos de pago`, `#conceptos libres`, `#clientes`, `#ventas`). This is the product backlog and the source of truth for scope decisions. Some stories there are still pending implementation (e.g., role-based authorization, deposit purchase returns, simplex/AI-driven stock allocation by margin, ticket/invoice generation, client CRUD).
- `api-tests.sh` — bash + curl + python3 end-to-end harness against `http://localhost:8080/api/v1`. Run with `bash api-tests.sh` after starting the server. It is the executable acceptance spec for the REST surface and includes a full purchase->inventory->sale flow plus a parent/child "box opening" flow. Section layout and helpers are documented in CLAUDE.md.

When the user adds requirements in conversation, expect them to land in `requisitos.txt` first and in `api-tests.sh` once the endpoint is wired.
