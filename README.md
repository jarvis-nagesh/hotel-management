# Hotel Management App (React + Spring Boot)

A full-stack app to manage hotel/restaurant tables:
- Add menu items with price.
- Start multiple table sessions in parallel.
- Validation: same table number cannot be started again while already active.
- Ongoing tables shown on right side.
- Click a table to open modal and add ordered items.
- Serve table and print bill with total.

## Tech Stack
- Frontend: React (Vite)
- Backend: Spring Boot

## Project Structure
- `frontend/` React UI
- `backend/` Spring Boot REST API

## Run Backend
```bash
cd backend
mvn spring-boot:run
```
Runs on `http://localhost:8080`.

## Run Frontend
```bash
cd frontend
npm install
npm run dev
```
Runs on `http://localhost:5173`.

## API Endpoints
- `GET /api/menu`
- `POST /api/menu`
- `GET /api/tables/active`
- `POST /api/tables/start`
- `POST /api/tables/{tableOrderId}/items`
- `POST /api/tables/{tableOrderId}/serve`
- `GET /api/tables/{tableOrderId}/bill`
