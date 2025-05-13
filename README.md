This guide will walk you through the steps to launch the project. Follow these instructions carefully to set up and run the project successfully.

## Prerequisites

Ensure you have the following installed on your system:

- Docker
- Compose
- Development Kit (JDK)
- npm
- Maven

## How to setup

### Quick Launch Using the Existing Docker Setup

If you don't want to change anything, the quick launch can be done by simply starting Docker Compose with the following command:

```bash
docker-compose up
```

This method is best for a stable version of the project and uses the existing Docker setup without any modifications.

#### Setting Up the Database

We will use Docker to set up a MySQL database. The `docker-compose.yml` file provided will help you configure and launch the database container.

##### docker-compose.yml

```yaml
services:
  db:
    image: mysql
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: trainer
    healthcheck:
      test: [ "CMD", "mysqladmin" ,"ping", "-h", "localhost" ]
      interval: 5s
      timeout: 5s
      retries: 10
    ports:
      - "127.0.0.1:3306:3306"
```

#### Configuring the Spring Boot Application

The `application.properties` file contains the configuration for the Spring Boot application. This file should be located in the `src/main/resources` directory of the **backend** project. Make sure the file includes the following settings:

##### application.properties

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/trainer
spring.datasource.username=root
spring.datasource.password=root
spring.jpa.hibernate.ddl-auto=update
server.port=8081

auth.clientId=[CLIENT_ID]
auth.clientSecret=[CLIENT_SECRET]
auth.redirectUri=http://localhost:8080/login

notification.student=5
notification.teacher=11
```

#### Authentication Configuration

You need to create your own client ID and secret for authentication. Follow these steps to obtain them:

1. Go to the Authentication Manager: [APPS MANAGER](https://auth.fit.cvut.cz/manager/index.jsf)
2. Create a New App:
   - Click on `New Project > Create App`.
   - Select `Web Application`.
   - Set the `Redirection URI` to `http://localhost:8080/login`.
3. Activate the Usermap API:
   - Go to `Services > UsermapAPI`.
   - Click `cvut:umapi:read` and activate it.
4. Copy the generated Client ID and Secret and replace `[CLIENT_ID]` and `[CLIENT_SECRET]` in your `application.properties` file with these values.

## Running the project

1. Run the database

   ```bash
   docker-compose up
   ```
2. Run the backend

   ```bash
   mvn spring-boot:run
   ```
3. Install frontend dependencies (first time only):

   ```bash
   npm install
   ```
4. Run the frontend server:

   ```bash
   npm run serve
   ```

## Accessing the Application

Once both the backend and frontend are running, you can access the application in your web browser at `http://localhost:8080`
