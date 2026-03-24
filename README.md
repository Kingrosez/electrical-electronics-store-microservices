# Electrical Electronics Store Microservices

A microservices-based e-commerce platform for electrical and electronics products built using Java (Spring Boot) and React.

---

## 🚀 Tech Stack

### Backend
- Java
- Spring Boot
- Spring Cloud (Gateway, Eureka, Config Server)
- JPA / Hibernate

### Frontend
- React.js
- Axios

### Database
- MySQL (Production)
- H2 (Development & Testing)

### Tools
- Maven
- Git & GitHub
- Postman

---

## 🏗️ Architecture

This project follows **Microservices Architecture** with clear separation of responsibilities.

- API Gateway for routing
- Service Discovery (Eureka)
- Centralized Configuration (Config Server)
- Independent services with separate databases

---

## 📦 Services

### Core Services
- Auth Service – authentication & JWT
- User Service – user profile management
- Product Service – product catalog
- Inventory Service – stock management
- Cart Service – cart operations
- Order Service – order processing
- Payment Service – payment handling

### Infrastructure Services
- API Gateway
- Config Server
- Service Registry (Eureka)

---

## 🔄 Application Flow

1. User registers or logs in  
2. User browses products  
3. User adds items to cart  
4. User places an order  
5. Payment is processed  
6. Order is confirmed and inventory is updated  

---

## 📁 Project Structure
electrical-electronics-store-microservices/
│
├── auth-service/
├── user-service/
├── product-service/
├── inventory-service/
├── cart-service/
├── order-service/
├── payment-service/
│
├── api-gateway/
├── config-server/
├── service-registry/
│
├── frontend/
│
├── docker-compose.yml (to be added)
└── README.md


---

## ⚙️ How to Run (Initial Setup)

```bash
# Clone the repository
git clone <your-repo-url>

# Navigate into project
cd electrical-electronics-store-microservices


🧪 Testing
Unit Testing using JUnit & Mockito
Integration Testing using H2 Database
📌 Future Enhancements
Docker & Docker Compose
Kafka (Event-driven communication)
Notification Service (Email/SMS)
Monitoring (Prometheus, Grafana)
CI/CD Pipeline
👨‍💻 Author

Fairoz

⭐ Notes

This project is designed following industry-level standards with focus on:

Clean architecture
Scalability
Service isolation
Real-world e-commerce flow


