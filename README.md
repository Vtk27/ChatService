# ChatService

`ChatService` is a hands-on chat backend I built while studying messaging system design in *System Design Interview* by Alex Xu.

The project focuses on the core pieces behind a real-time chat application by wiring together HTTP APIs, WebSockets, Redis, persistence, and basic room/user coordination.

## What it does

- creates a chat room ID for a user
- lets another user join an existing chat
- opens a WebSocket connection for live messaging
- stores messages in PostgreSQL
- uses Redis for chat membership and pub/sub style message fan-out
- exposes a simple `/health` endpoint for uptime checks

## Tech stack

- Java 17
- Spring Boot
- Spring Web MVC
- Spring WebSocket
- Spring Data Redis
- PostgreSQL
- Docker / Docker Compose for local dependencies

## API overview

### HTTP endpoints

- `POST /chat-id`
  - creates a chat room
- `GET /chat/join?chatId=...&userId=...`
  - joins an existing chat
- `GET /messages?chatId=...&limit=...`
  - fetches recent messages
- `GET /health`
  - simple health endpoint

### WebSocket endpoint

- `/ws?userId=...&chatId=...`

## How it works at a high level

When a user creates or joins a chat, the service keeps lightweight chat membership data in Redis. Once connected over WebSocket, messages are sent in real time and also persisted to PostgreSQL so chat history can be fetched later. Redis is used as the coordination layer for active chat participants, while PostgreSQL acts as the longer-lived message store.

## Running locally

You will need:

- Java 17
- PostgreSQL
- Redis

The project also includes Docker-related files that can help with local setup.

Typical steps:

```bash
./mvnw spring-boot:run
```

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

You will also need to configure the required environment variables or application properties for:

- database connection
- Redis connection
- allowed frontend origins for CORS

## Why I built this

I wanted to move beyond reading system design and actually implement a messaging backend myself. Alex Xu's discussion of chat and messaging systems was the push to turn those ideas into code and understand the tradeoffs more concretely.

So this repository is best read as:

- a hands-on learning exercise
- a simple real-time chat backend
- a place where system design ideas were tested in code

## Notes

- this project is still evolving
- the frontend lives separately in the broader `ChatSystem` workspace
