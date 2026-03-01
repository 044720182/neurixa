# Neurixa Blog Module: A Developer's Guide

Welcome to the Neurixa Blog Module! This guide is designed for new developers to understand the architecture, philosophy, and features of our WordPress-like blogging engine.

## 1. Core Philosophy: Clean & Maintainable

Our primary goal is to build software that is easy to understand, maintain, and extend. We achieve this by following a strict **Layered Modular Monolith** architecture, inspired by Clean Architecture and Hexagonal (Ports & Adapters) Architecture.

**Key Principles:**

*   **Separation of Concerns:** Each layer has a single, well-defined responsibility.
*   **Dependency Inversion:** Dependencies always point inward, from outer layers to inner layers. The core business logic (Domain) knows nothing about the database or the web server.
*   **Explicitness over Magic:** We prefer clear, explicit code over "magical" frameworks that hide complexity.
*   **Testability:** The architecture is designed to be easily testable at every level.

## 2. The Four Layers of the Blog Module

Our code is organized into four primary layers, each residing in its own module:

![Layered Architecture Diagram](https://i.imgur.com/8S1t3P5.png)

#### 🌳 **`neurixa-domain` (The Core)**

*   **What it is:** The heart of the application. It contains the core business logic, rules, and entities.
*   **Contents:**
    *   **Entities:** Plain Java Objects (POJOs) representing our business concepts (`Article`, `Category`, `Comment`). This is where business rules live (e.g., `article.publish()`).
    *   **Value Objects:** Immutable objects representing simple values (e.g., `Slug`).
    *   **Repository Interfaces:** Contracts that define what the application needs from the database (e.g., `ArticleRepository`), but *not* how the database does it.
*   **Golden Rule:** This layer has **zero** dependencies on any framework (like Spring) or external system (like MongoDB). It's pure, unadulterated Java.

#### ⚙️ **`neurixa-application` (The Use Cases)**

*   **What it is:** The orchestrator. It defines all the specific actions a user can perform.
*   **Contents:**
    *   **Use Case Classes:** Each class represents a single user action (e.g., `CreateArticleUseCase`, `PublishArticleUseCase`).
    *   **Logic:** Use cases coordinate the flow of data. They fetch entities from repositories, call their business methods, and save them back.
*   **Golden Rule:** This layer depends only on the `neurixa-domain` layer. It knows nothing about the web or the database implementation.

#### 🔌 **`neurixa-adapter` (The Infrastructure)**

*   **What it is:** The bridge to the outside world. It provides concrete implementations for the interfaces defined in the domain layer.
*   **Contents:**
    *   **Repository Implementations:** Classes that implement the repository interfaces (e.g., `ArticleRepositoryImpl`) using a specific technology like MongoDB.
    *   **Database Documents:** Data Transfer Objects (DTOs) that map to our MongoDB collections (e.g., `ArticleDocument`).
    *   **Mappers:** Classes responsible for converting between domain entities and database documents.
*   **Golden Rule:** This layer "adapts" our core application to specific technologies. If we wanted to switch from MongoDB to PostgreSQL, we would only need to change this layer.

#### 🌐 **`neurixa-boot` (The Delivery Mechanism)**

*   **What it is:** The entry point. It exposes the application to the outside world.
*   **Contents:**
    *   **REST Controllers:** Thin classes that handle HTTP requests, call the appropriate use cases, and return HTTP responses.
    *   **Configuration:** Spring Boot configuration classes that wire all the components together (e.g., `BlogUseCaseConfiguration`).
*   **Golden Rule:** Controllers contain **no business logic**. Their only job is to manage HTTP traffic.

## 3. Feature Deep Dive

### Article (The "Post")

The `Article` is the central entity. It's an **Aggregate Root**, meaning it manages the lifecycle of other related objects.

*   **Statuses:** An article moves through a strict lifecycle:
    1.  `DRAFT`: Initial state. Can be updated freely.
    2.  `PUBLISHED`: Visible to the public. Cannot be deleted directly.
    3.  `ARCHIVED`: No longer listed publicly but kept for records. Can be deleted from here.
    4.  `DELETED`: Soft-deleted. Can be restored to `DRAFT`.
*   **Slug:** A URL-friendly version of the title (e.g., "Hello World" -> "hello-world") is automatically generated.
*   **Relationships:** An article can have many `Categories` and `Tags`.

### Category & Tag

*   **Category:** A hierarchical way to group articles (e.g., "Technology" -> "Programming").
*   **Tag:** A non-hierarchical keyword to label articles (e.g., "java", "spring-boot").

### Comment

*   **Statuses:** Comments also have a lifecycle to prevent spam:
    1.  `PENDING`: Awaits moderation. Not visible publicly.
    2.  `APPROVED`: Visible to the public.
    3.  `REJECTED`: Spam or inappropriate.
    4.  `DELETED`: Soft-deleted.
*   **Nesting:** Comments support one level of replies via the `replyTo` field.

## 4. REST API Guide

Here are the primary endpoints for interacting with the blog module.

**Base Path:** `/api/blog`

### Articles (`/api/blog/articles`)

*   **`POST /` - Create a Draft Article**
    *   Creates a new article in `DRAFT` status.
    *   **Body:** `{"title": "My New Post", "content": "...", "excerpt": "..."}`
    *   **cURL Example:**
        ```bash
        curl -X POST http://localhost:8080/api/blog/articles \
             -H "Content-Type: application/json" \
             -d '{"title":"My First Post", "content":"This is the content.", "excerpt":"A short summary."}'
        ```

*   **`PUT /{id}` - Update an Article**
    *   Updates the title, content, or excerpt of a draft article.
    *   **Body:** `{"title": "Updated Title", "content": "...", "excerpt": "..."}`

*   **`POST /{id}/publish` - Publish an Article**
    *   Changes an article's status from `DRAFT` to `PUBLISHED`.
    *   **cURL Example:**
        ```bash
        curl -X POST http://localhost:8080/api/blog/articles/YOUR_ARTICLE_ID/publish
        ```

*   **`DELETE /{id}` - Delete an Article**
    *   Soft-deletes an article (moves it to `DELETED` status). The article must not be `PUBLISHED`.

*   **`GET /{slug}` - Get a Published Article**
    *   Fetches a single published article by its slug for public viewing.

*   **`GET /` - List Published Articles**
    *   Returns a list of all articles with `PUBLISHED` status.

### Comments (`/api/blog/comments`)

*   **`POST /` - Add a Comment**
    *   Adds a new comment to an article. It will be `PENDING` by default.
    *   **Body:** `{"articleId": "...", "authorName": "...", "authorEmail": "...", "content": "..."}`
    *   **cURL Example:**
        ```bash
        curl -X POST http://localhost:8080/api/blog/comments \
             -H "Content-Type: application/json" \
             -d '{"articleId":"YOUR_ARTICLE_ID", "authorName":"John Doe", "authorEmail":"john@example.com", "content":"Great article!"}'
        ```

*   **`POST /{id}/approve` - Approve a Comment**
    *   Changes a comment's status from `PENDING` to `APPROVED`.

*   **`POST /{id}/reject` - Reject a Comment**
    *   Changes a comment's status from `PENDING` to `REJECTED`.

### Categories & Tags

*   **`POST /api/blog/categories`**: Creates a new category.
*   **`POST /api/blog/tags`**: Creates a new tag.

## 5. How to Extend the Module

Imagine you want to add a "like" feature to articles. Here’s how you’d do it following our architecture:

1.  **Domain:** Add a `likeCount` field and an `incrementLikes()` method to the `Article` entity.
2.  **Application:** Create a new `LikeArticleUseCase` that fetches the article, calls `incrementLikes()`, and saves it.
3.  **Boot:** Add a new endpoint `POST /api/blog/articles/{id}/like` to the `BlogArticleController` that calls your new use case.

Notice you wouldn't need to touch the adapter layer at all! This is the power of clean, separated architecture.
