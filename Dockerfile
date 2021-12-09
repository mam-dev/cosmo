FROM maven:3-jdk-8

WORKDIR /usr/src/mymaven
# COPY ./pom.xml ./pom.xml
# COPY ./cosmo-api/pom.xml ./cosmo-api/pom.xml
# COPY ./cosmo-core/pom.xml ./cosmo-core/pom.xml
# COPY ./cosmo-webapp/pom.xml ./cosmo-webapp/pom.xml
# RUN mvn dependency:go-offline -B

COPY . .
RUN mvn clean install

WORKDIR /usr/src/mymaven/cosmo-webapp
CMD ["mvn", "spring-boot:run"]
