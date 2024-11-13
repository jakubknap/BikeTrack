# BikeTrack
>BikeTrack is an application that assists users in keeping detailed records of repairs and taking care of their bicycles, offering functionalities such as bike management, recording and editing repairs, and monitoring maintenance costs

Key features of the application:
- User support: register and log in users, activate account with email code
- Bike management: add, edit and delete bikes in user's “garage”
- Repair management: add, edit and delete repairs assigned to a bike, view repair history of a specific bike or all repairs
- Repair statistics: generate statistics on repair costs and number of repairs for user's bikes

## Table of Contents
* [Technologies Used](#technologies-used)
* [Setup](#setup)
* [Contact](#contact)

## Technologies Used
- Spring Boot 3
- Spring Security 6
- JWT Token Authentication
- Spring Data JPA
- JSR-303 and Spring Validation
- OpenAPI and Swagger UI Documentation
- Docker

## Setup
1. Clone the repository:
```bash
   git clone https://github.com/jakubknap/BikeTrack.git
```
2. Run the application
```bash
  mvn spring-boot:run
```

## API
- You can check the functionality of the backend itself using swagger: http://localhost:8088/swagger-ui.html  Important! Remember about authentication

## Contact
Created by [Jakub Knap](https://www.linkedin.com/in/jakub-knap/) - feel free to contact me!

