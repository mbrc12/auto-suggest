FROM openjdk:11-jre
WORKDIR /code
COPY build/libs/*.jar auto-suggest.jar
ENV spring_profiles_active=docker
EXPOSE 8090
CMD java -jar auto-suggest.jar
