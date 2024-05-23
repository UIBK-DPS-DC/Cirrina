# Stage 1: Build Stage
FROM gradle:8.7.0-jdk21-alpine AS build

# Install protoc
RUN apk add protoc

# Copy application source code into the container
COPY --chown=gradle:gradle . /usr/src/cirrina

# Set working directory
WORKDIR /usr/src/cirrina

# Build the application distribution ZIP
RUN gradle distZip

# Stage 2: Runtime Stage
FROM openjdk:21-bookworm

# Define a build argument for the application version
ARG CIRRINA_VERSION="cirrina-1.0-SNAPSHOT"

# Copy the application distribution ZIP from the build stage
COPY --from=build /usr/src/cirrina/build/distributions/${CIRRINA_VERSION}.zip /tmp/${CIRRINA_VERSION}.zip

# Unzip the application distribution to /usr/bin
RUN unzip /tmp/${CIRRINA_VERSION}.zip -d /usr/bin \
    && mv /usr/bin/${CIRRINA_VERSION} /usr/bin/cirrina \
    && chmod +x /usr/bin/cirrina/bin/cirrina

# Set the working directory for the application
WORKDIR /usr/bin/cirrina

# Use shell form ENTRYPOINT to execute the application
ENTRYPOINT ["/bin/sh", "-c", "/usr/bin/cirrina/bin/cirrina"]