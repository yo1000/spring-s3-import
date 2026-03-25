FROM amazoncorretto:25

ARG JAR

COPY $JAR /opt/app.jar

ENTRYPOINT ["java", "-jar", "/opt/app.jar"]
