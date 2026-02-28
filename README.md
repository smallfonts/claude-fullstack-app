# DeployTrack — Artifact Deployment Dashboard

A full-stack dashboard for tracking and triggering application artifact deployments via IBM UrbanCode uDeploy.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 21, Spring Boot 3, Gradle |
| Frontend | Vue 3, Vite, Pinia, Vue Router |
| Database | H2 (dev) / MySQL (prod) |
| Integration | IBM UrbanCode uDeploy REST API |

## Project Structure

```
claude-fullstack-app/
├── backend/          # Spring Boot Gradle project
│   └── src/
│       └── main/java/com/deploytrack/
│           ├── model/          # JPA entities
│           ├── repository/     # Spring Data repositories
│           ├── service/        # Business logic + uDeploy integration
│           ├── controller/     # REST controllers
│           ├── dto/            # Request/Response DTOs
│           ├── enums/          # Role, DeploymentStatus enums
│           └── config/         # Security, CORS, uDeploy config
└── frontend/         # Vue 3 + Vite app
    └── src/
        ├── views/              # Page-level components
        ├── components/         # Reusable UI components
        ├── stores/             # Pinia state stores
        ├── services/           # API service layer
        └── router/             # Vue Router config
```

## Getting Started

### Backend

```bash
cd backend
./gradlew bootRun
```

API runs on `http://localhost:8080`

### Frontend

```bash
cd frontend
npm install
npm run dev
```

App runs on `http://localhost:5173`

## Configuration

Copy `backend/src/main/resources/application.properties` and adjust:
- `udeploy.base-url` — your uDeploy server URL
- `udeploy.auth-token` — your uDeploy API token
- Database connection settings for production

## User Roles

| Role | Permissions |
|------|------------|
| `READ_ONLY` | View deployment requests and history |
| `DEPLOYMENT_REQUESTOR` | Create deployment requests |
| `DEPLOYMENT_APPROVER` | Approve/reject and trigger deployments |
