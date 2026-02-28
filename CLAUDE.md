# DeployTrack — CLAUDE.md

## Project Overview
Full-stack artifact deployment dashboard for tracking and triggering deployments via IBM UrbanCode uDeploy. Monorepo with separate `backend/` (Spring Boot) and `frontend/` (Vue 3) directories.

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Spring Boot 3.2.4, Java 21, Gradle |
| Frontend | Vue 3, Vite 5, Pinia, Vue Router 4, Axios |
| DB (dev) | H2 in-memory |
| DB (prod) | MySQL |
| Integration | IBM UrbanCode uDeploy (WebFlux reactive client) |

## Running the App

### Backend
```bash
cd backend
./gradlew bootRun
# API: http://localhost:8080
# H2 Console: http://localhost:8080/h2-console
```

### Frontend
```bash
cd frontend
npm install
npm run dev
# App: http://localhost:5173 (proxies /api/* to :8080)
```

### Frontend scripts
```bash
npm run dev      # dev server with HMR
npm run build    # production build
npm run preview  # preview production build
```

## Backend Structure
```
backend/src/main/java/com/deploytrack/
├── config/         # SecurityConfig, UDeployWebClientConfig, UDeployProperties
├── controller/     # DeploymentRequestController, UDeployController, UserController
├── dto/            # Request/response DTOs
├── enums/          # UserRole, DeploymentStatus, DeploymentMethodType
├── model/          # JPA entities: User, DeploymentRequest, DeploymentMethod, ComponentArtifact
├── repository/     # Spring Data repos (4 total)
└── service/        # DeploymentRequestService, UDeployService
```

## Frontend Structure
```
frontend/src/
├── router/         # Vue Router config
├── services/       # Axios API client layer
├── stores/         # Pinia stores (deploymentStore.js)
└── views/          # DashboardView, DeploymentRequestsView, ArtifactsView,
                    # NewRequestView, RequestDetailView
```

## Key Configuration (`backend/src/main/resources/application.properties`)
- `server.port=8080`
- `spring.datasource.url=jdbc:h2:mem:deploytrack`
- `spring.h2.console.enabled=true`
- `spring.jpa.hibernate.ddl-auto=update`
- `udeploy.base-url=https://your-udeploy-server.example.com`
- `udeploy.auth-token=YOUR_UDEPLOY_API_TOKEN`
- `udeploy.ssl-verify=false` (dev only)
- `cors.allowed-origins=http://localhost:5173`

## User Roles
- `READ_ONLY` — view deployments only
- `DEPLOYMENT_REQUESTOR` — create deployment requests
- `DEPLOYMENT_APPROVER` — approve and trigger deployments

## Architecture Notes
- Frontend dev server proxies `/api/*` to `http://localhost:8080` (configured in `vite.config.js`)
- uDeploy integration uses Spring WebFlux for async HTTP calls
- Spring Security enforces role-based access control
- H2 in-memory DB for dev; MySQL driver bundled for production switch
- No Docker setup exists yet
