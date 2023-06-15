# Project Readme

## Introduction
This project is a service registration gateway. It allows you to manage (create, read, update, delete - CRUD) registered services and their configurations. The APIs are developed with Spring Boot and use a reactive programming model with Spring WebFlux and Reactor.

## Prerequisites
Before running this project, ensure you have the following software installed on your machine:

- Make
- Docker
- Docker Compose
- Maven
- Java 17

## Building the Project
To build the project, open a terminal in the project's root directory and run:

```bash
make build
```

## Running the Project
To run the project, use the command:

```bash
make run
```

## API Usage

Here is a brief introduction to each of the endpoints provided by the `ServiceRegistrationController`:

- **GET /service-registration:** Retrieves all registered services.

```bash
curl -X GET http://localhost:8080/service-registration
```

- **POST /service-registration:** Registers a new service. The service details are provided in the request body as JSON.

```bash
curl -X POST -H "Content-Type: application/json" -d '{ "id": "service1", "registeredServiceConfigurations": { "servers": [{"protocol":"http", "host":"localhost", "port":8081}], "allowCache": true, "ttl": 3600 } }' http://localhost:8080/service-registration
```

- **GET /service-registration/{id}:** Retrieves the details of the registered service identified by `{id}`.

```bash
curl -X GET http://localhost:8080/service-registration/service1
```

- **PUT /service-registration/{id}:** Updates the configuration of the registered service identified by `{id}`.

```bash
curl -X PUT -H "Content-Type: application/json" -d '{ "servers": [{"protocol":"http", "host":"localhost", "port":8082}], "allowCache": false, "ttl": 7200 }' http://localhost:8080/service-registration/service1
```

- **DELETE /service-registration/{id}:** Deletes the registered service identified by `{id}`.

```bash
curl -X DELETE http://localhost:8080/service-registration/service1
```

## Cleaning Up
If you need to clean up Docker (remove containers, networks, and images), use the following command:

```bash
make clean
```

## Stopping the Project
If you wish to stop the running project, you can use the following command:

```bash
make stop
```

## Refreshing the Project
If you want to fully refresh your project (clean, build, and run), use the following command:

```bash
make refresh
```

## swagger-ui
The project also includes a swagger-ui interface for interacting with the APIs. To access the swagger-ui interface, navigate to http://localhost:8080/swagger-ui.html
in your browser.
