# Neurixa Blog v2 – Hardened Design & Markdown Report

## Overview
- Neurixa Blog v2 upgrades the existing blog feature while preserving Clean Architecture boundaries across neurixa-domain, neurixa-application, neurixa-adapter, and neurixa-boot.
- The refactor strengthens the domain model, introduces immutable value objects, prepares internal domain events, hardens soft delete and audit strategies, and adds an adapter-level atomic view counter.
- Backward compatibility is maintained at the REST API level via controller DTOs; endpoints and request/response shapes remain stable.

## Problems in v1
- Anemic domain entities with behavior dispersed into use cases.
- UUID used directly in the domain; weak invariants and limited typing.
- No consistent soft delete fields; mixed status usage without deleted/deletedAt tracking.
- Domain events existed but were not integrated into aggregates.
- Adapter repository implementations were incomplete; missing slug queries and atomic counters.
- Use cases led to proliferation of one-class-per-action without clear command/query separation.
- Mapping from persistence to domain was unimplemented, risking leakage or brittle reconstruction.

## Hardened Improvements
- Rich domain model: Article and Comment encapsulate all state transitions; Tag and Category follow clear invariants with immutable Slug handling.
- Value objects: ArticleId, CategoryId, TagId, CommentId used within domain for stronger typing and invariants.
- Domain events integrated through BaseAggregateRoot with event collection and pull semantics.
- Soft delete standardized with deleted and deletedAt across aggregates, enforced by repositories.
- Audit fields createdAt/updatedAt consistently maintained in entities.
- Atomic view increments implemented in the adapter using MongoDB $inc via MongoTemplate.
- Command vs Query services introduced to reduce class explosion and clarify intent.
- Repository implementations completed with filtering and slug-based lookups.
- Controller DTOs maintain backward-compatible API responses while domain remains framework-free.

## Architectural Decisions
- Domain-only behavior: All business rules and validations are enforced inside entity methods (publish, archive, restore, updateContent, approve, reject).
- Value objects for IDs: Domain depends on typed identifiers; conversion to UUID occurs only in adapters and boot DTOs.
- Event model: Aggregates extend BaseAggregateRoot and register lightweight events (ArticlePublishedEvent, CommentApprovedEvent) without external dispatch at this stage.
- Persistence isolation: Mongo dependencies are confined to neurixa-adapter; mapping reconstructs pure domain via fromState factories.
- DTOs at the boundary: Controllers convert domain entities into stable response DTOs to preserve API contracts.

## Domain Model Explanation
- Article: Aggregate root with methods publish, archive, restore, updateContent, updateSeo, changeFeaturedImage, assignCategory/tag, removeCategory/tag, incrementViewCount. Holds ArticleId, Slug, status, audit, soft delete, and category/tag IDs.
- Comment: Aggregate with approve, reject, softDelete, audit, and soft delete fields; references ArticleId, optional reply CommentId.
- Category: Entity with name, Slug, optional parent CategoryId, audit, soft delete.
- Tag: Entity with name, Slug, audit, soft delete.
- Invariants: Non-empty titles/names, content required for publish, illegal transitions guarded (e.g., delete published without archive).

## Event Model Explanation
- DomainEvent interface expresses occurredOn timestamp.
- BaseAggregateRoot provides registerEvent and pullDomainEvents for internal event collection.
- Article emits ArticlePublishedEvent on publish; Comment emits CommentApprovedEvent on approve.
- Events are internal to the domain and not coupled to any transport; external messaging can be integrated later via an outbox or publisher at adapter/application layers.

## Concurrency Strategy
- Domain method incrementViewCount() remains for local state semantics.
- Adapter implements atomic increment via MongoDB $inc using MongoTemplate updateFirst on the article document by _id.
- Application services and legacy use case IncrementViewCountUseCase route increments through repository.incrementViewCountAtomic to avoid race conditions.
- Rationale: Keeps domain pure and portable while ensuring correctness at the persistence boundary; adapter provides single-source-of-truth counter increments.

## Soft Delete Strategy
- Entities include deleted (boolean) and deletedAt (Instant).
- Repository queries automatically filter out deleted items via Criteria deleted != true.
- Article also retains status DELETED for state machine clarity; deleted/deletedAt standardize cross-aggregate deletion semantics.
- Restore clears deleted and deletedAt while returning the entity to a valid lifecycle state (e.g., DRAFT for Article).

## Trade-offs
- Introducing DTOs for controllers adds a mapping step but preserves stable APIs without contaminating the domain with serialization concerns.
- Value objects improve correctness but require additional mapping at the adapter/boot boundaries.
- Event model is internal-only for now; no external messaging means side effects are not automatically propagated; keeps scope controlled and aligned with the objective.
- Command/query consolidation reduces class count and improves coherence but shifts some responsibilities into broader service classes.

## Future Scalability Roadmap
- Implement an outbox/event publisher in neurixa-adapter to persist domain events and dispatch to messaging infrastructure (Kafka/RabbitMQ) reliably.
- Add pagination, filtering, and sorting in query services for list endpoints; expose dedicated read models if needed.
- Introduce read-side projections for fast queries (e.g., published article summaries).
- Add optimistic locking/version fields to documents for update contention.
- Expand validation with richer value objects (Email, Title) and specification-based policies.
- Integrate caching for hot paths (read models, slug lookups) with Redis while keeping cache outside the domain.
- Enhance testing: property-based tests for Slug, event emission contract tests, and repository integration tests with embedded Mongo.
