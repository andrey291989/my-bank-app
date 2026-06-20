#!/bin/bash

# Script to build Docker images for all microservices

echo "Building Docker images for all microservices..."

# Build front-ui
echo "Building front-ui..."
docker build -t bank/front-ui:latest ./front-ui

# Build gateway
echo "Building gateway..."
docker build -t bank/gateway:latest ./gateway

# Build accounts-service
echo "Building accounts-service..."
docker build -t bank/accounts-service:latest ./accounts-service

# Build cash-service
echo "Building cash-service..."
docker build -t bank/cash-service:latest ./cash-service

# Build transfer-service
echo "Building transfer-service..."
docker build -t bank/transfer-service:latest ./transfer-service

# Build notifications-service
echo "Building notifications-service..."
docker build -t bank/notifications-service:latest ./notifications-service

echo "All Docker images built successfully!"