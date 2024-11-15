FROM ubuntu:18.04

RUN apt-get update && apt-get install -y default-jdk curl tar && apt-get clean


RUN curl -fsSL https://downloads.apache.org/maven/maven-3/3.8.8/binaries/apache-maven-3.8.8-bin.tar.gz | tar -xz -C /opt && \
    ln -s /opt/apache-maven-3.8.8/bin/mvn /usr/bin/mvn

# Verify Maven version
RUN mvn --version

WORKDIR /app

COPY . .

#Change directory to the one with pom.xml
WORKDIR /app/twentythree


#Build the project
RUN mvn clean install

#Copy the built jar to the docker image
RUN cp /app/twentythree/target/twentythree-1.0-SNAPSHOT.jar /app/twentythree.jar


#Run the jar
CMD ["java", "-jar", "/app/twentythree.jar"]
