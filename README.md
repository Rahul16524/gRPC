# spring-grpc-example

## Description
This is a simple example of how to use gRPC with Spring Boot.

# Spring Boot Microservices with gRPC

## Overview

This project demonstrates communication between Spring Boot microservices using **gRPC**.

The project contains three services:

- **Configuration Service** – Builds and installs the shared configuration/dependency module.
- **Payment Service** – Handles payment-related operations and provides gRPC functionality.
- **Order Service** – Exposes REST APIs for order and order-item operations and communicates with the Payment Service using gRPC.

## Technologies Used

- Java 17
- Spring Boot
- Spring Cloud
- gRPC
- Protocol Buffers
- Maven
- REST APIs
- Postman

## Prerequisites

Make sure the following are installed:

- JDK 17
- Apache Maven
- Git
- Postman (for API testing)

Verify Java and Maven:

```bash
java -version
mvn -version
git --version
```

## 1. Clone the Project

Open CMD or a terminal and execute:

```bash
git clone --recurse-submodules https://github.com/Rahul16524/gRPC.git spring-grpc-test
```

Navigate into the project:

```bash
cd spring-grpc-test
```

The `--recurse-submodules` option downloads the main repository along with its submodules.

**Project structure:**

```text
spring-grpc-test/
│
├── configuration-service/
│
├── payment-service/
│
├── order-service/
│
├── pom.xml
└── README.md
```

## 2. Build and Run the Applications

Follow the execution order below.

### Step 1: Configuration Service

Open the first terminal and navigate to the configuration service:

```bash
cd configuration-service
```

Build and install the project:

```bash
mvn clean install
```

Wait until Maven displays:

```text
BUILD SUCCESS
```

**Configuration Service – Console Output**

<!-- Add configuration-service build success screenshot here -->
![alt text](<Screenshot (419).png>)

![Configuration Service Console Output](screenshots/configuration-service.png)

### Step 2: Payment Service

Navigate to the payment service from the project root:

```bash
cd ..
cd payment-service
```

Compile the project:

```bash
mvn clean compile
```

Start the Payment Service:

```bash
mvn spring-boot:run
```

Wait until the application starts successfully. Keep this terminal open.

**Payment Service – Console Output**

<!-- Add payment-service startup screenshot here -->
![alt text](<Screenshot (422).png>) ![alt text](<Screenshot (421).png>)

![Payment Service Console Output](screenshots/payment-service.png)

### Step 3: Order Service

Open a **second terminal**. Navigate to the order service:

```bash
cd /path/to/spring-grpc-test/order-service
```

Replace `/path/to/spring-grpc-test` with your actual project directory.

For example, on Windows:

```cmd
cd /d R:\Project\SpringRPC\spring-grpc-test\order-service
```

Compile the project:

```bash
mvn clean compile
```

Start the Order Service:

```bash
mvn spring-boot:run
```

Wait until the application starts successfully. Keep this terminal open.

**Order Service – Console Output**

<!-- Add order-service startup screenshot here -->
![alt text](<Screenshot (423).png>)
![alt text](<Screenshot (424).png>)
![Order Service Console Output](screenshots/order-service.png)

## 3. Application Execution Order

Start the applications in this order:

| Order | Service | Command | Purpose |
|---|---|---|---|
| 1 | Configuration Service | `mvn clean install` | Build and install |
| 2 | Payment Service | `mvn clean compile` | Compile |
| 3 | Payment Service | `mvn spring-boot:run` | Start application |
| 4 | Order Service | `mvn clean compile` | Compile |
| 5 | Order Service | `mvn spring-boot:run` | Start application |

**Important:** Start the Payment Service before the Order Service because the Order Service communicates with it using gRPC.

The Order Service exposes REST endpoints on:

```text
http://localhost:8009
```

## 4. REST API Testing

Use Postman to test the following endpoints after both applications have started successfully.

### API 1: Create Order

**Method:** POST

**Endpoint:**

```http
http://localhost:8009/api/order
```

**Request Body:**

```json
{
  "customerId": 588,
  "items": [
    {
      "productId": 788,
      "quantity": 7,
      "unitPrice": 4.2
    }
  ]
}
```

**Postman Screenshot:**

<!-- Add create-order API screenshot here -->

![Create Order API](screenshots/create-order-api.png)

### API 2: Make Payment for an Order

**Method:** POST

**Endpoint:**

```http
http://localhost:8009/api/order/payment/1
```

This endpoint initiates payment for the order with ID `1`.

**Postman Screenshot:**

<!-- Add order-payment API screenshot here -->

![Order Payment API](screenshots/order-payment-api.png)

### API 3: Create Multiple Order Items

**Method:** POST

**Endpoint:**

```http
http://localhost:8009/api/order-item/list
```

**Request Body:**

```json
[
  {
    "productId": 634,
    "quantity": 9,
    "unitPrice": 92
  },
  {
    "productId": 699,
    "quantity": 3,
    "unitPrice": 8.3
  }
]
```

**Expected Output (example):**

```json
[
  {
    "id": 4,
    "productId": 634,
    "quantity": 9,
    "unitPrice": 92.0,
    "totalAmount": 828.0
  },
  {
    "id": 5,
    "productId": 699,
    "quantity": 3,
    "unitPrice": 8.3,
    "totalAmount": 24.900000000000002
  }
]
```

**Postman Screenshot:**

<!-- Add multiple-order-items API screenshot here -->

![Create Multiple Order Items API](screenshots/create-order-items-api.png)

### API 4: Create a Single Order Item

**Method:** POST

**Endpoint:**

```http
http://localhost:8009/api/order-item
```

**Request Body:**

```json
{
  "productId": 822,
  "quantity": 4,
  "unitPrice": 7
}
```

**Expected Output (example):**

```json
{
  "id": 3,
  "productId": 822,
  "quantity": 4,
  "unitPrice": 7.0,
  "totalAmount": 28.0
}
```

**Postman Screenshot:**

<!-- Add single-order-item API screenshot here -->

![Create Single Order Item API](screenshots/create-order-item-api.png)

## 5. Screenshots Directory

Create a `screenshots` directory in the root project folder and save your screenshots using these names:

```text
screenshots/
│
├── configuration-service.png
├── payment-service.png
├── order-service.png
├── create-order-api.png
├── order-payment-api.png
├── create-order-items-api.png
└── create-order-item-api.png
```

These screenshots document the successful build, application startup, and REST API testing.

## 6. Important Notes

- Start the Payment Service before starting the Order Service.
- Keep both application terminals running while testing the REST APIs.
- The Order Service REST API runs on port `8009` in this example.
- The expected IDs in the API responses may differ depending on the existing database records and application state.
- If you encounter a connection error while making a payment, verify that the Payment Service is running and that the gRPC connection configuration is correct.
- If Maven compilation fails, check the Java version and Maven dependencies.

## Conclusion

This project demonstrates communication between Spring Boot microservices using gRPC, while exposing REST APIs for client-side interactions.

The Order Service accepts REST requests and communicates with the Payment Service through gRPC for inter-service operations.
