# Use a newer base image with ARM support
FROM ubuntu:20.04

# Set non-interactive frontend to avoid prompts during package installation
ENV DEBIAN_FRONTEND=noninteractive

# Update and install required packages
RUN apt-get update && \
    apt-get install -y \
    openjdk-11-jdk \
    curl \
    tar && \
    apt-get clean

# Download and install Maven
RUN curl -fsSL https://downloads.apache.org/maven/maven-3/3.8.8/binaries/apache-maven-3.8.8-bin.tar.gz | \
    tar -xz -C /opt && \
    ln -s /opt/apache-maven-3.8.8/bin/mvn /usr/bin/mvn

# Verify Maven installation
RUN mvn --version

# Set working directory
WORKDIR /app

# Copy application source files
COPY . .

# Change directory to the one with the pom.xml
WORKDIR /app/twentythree

# Build the project
RUN mvn clean install

# Return to the main working directory
WORKDIR /app

# Create a directory for the built JAR
RUN mkdir /app/bin

# Copy the built JAR to the bin directory
RUN cp /app/twentythree/target/twentythree-1.0-SNAPSHOT.jar /app/bin/twentythree.jar

# Ensure the script is executable
RUN chmod +x /app/run.sh

# Default command to run the application
CMD ["/app/run.sh"]
