FROM eclipse-temurin:21-jdk

COPY target/hubble-1.0.war app.war

ENTRYPOINT ["java","-jar","app.jar"]