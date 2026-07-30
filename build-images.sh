#!/bin/bash

# Script to build Docker images for all microservices.
# All Dockerfiles are multi-module Maven builds and expect the repository root
# as the build context (they COPY pom.xml, the module poms and shared-kafka).
# The script will exit immediately if any command fails.

set -e  # Exit immediately if a command exits with a non-zero status

echo "Building Docker images for all microservices..."

# Build front-ui
echo "Building front-ui..."
docker build -f front-ui/Dockerfile -t bank/front-ui:latest .

# Build gateway
echo "Building gateway..."
docker build -f gateway/Dockerfile -t bank/gateway:latest .

# Build accounts-service
echo "Building accounts-service..."
docker build -f accounts-service/Dockerfile -t bank/accounts-service:latest .

# Build cash-service
echo "Building cash-service..."
docker build -f cash-service/Dockerfile -t bank/cash-service:latest .

# Build transfer-service
echo "Building transfer-service..."
docker build -f transfer-service/Dockerfile -t bank/transfer-service:latest .

# Build notifications-service
echo "Building notifications-service..."
docker build -f notifications-service/Dockerfile -t bank/notifications-service:latest .

echo "All Docker images built successfully!"
