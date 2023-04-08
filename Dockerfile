FROM openjdk:11
COPY target/pace-1.0.jar pace-1.0.jar
ENTRYPOINT ["java","-jar","/pace-1.0.jar"]