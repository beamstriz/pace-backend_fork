FROM openjdk:11
COPY target/pace1.0.jar pace1.0.jar
ENTRYPOINT ["java","-jar","/pace-1.0.jar"]