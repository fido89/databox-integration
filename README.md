# databox-integration
A backend application that extract metrics from Twitter and Facebook and sends them to Databox

## Requirements
1. Java 14 (or newer)
2. Maven 3.6.3 (or newer)
3. Databox, Twitter and Facebook API credentials

## Usage
1. Set API credentials in application.properties file
2. Build the application using maven:
    ```bash
    mvn clean package
    ```
3. Run the application:
    ```batch
    mvn exec:java
    ```
4. Follow the authorization process in the console
