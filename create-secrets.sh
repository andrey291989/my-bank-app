#!/bin/bash

# Script to create all Kubernetes Secrets for the bank application
# NOTE: This script is for demonstration purposes only. In a real environment,
# you should replace the placeholder values with actual strong passwords.

echo "Creating Kubernetes Secrets for Bank App..."
echo "NOTE: Please replace the placeholder values with actual strong passwords."

# Create namespace if it doesn't exist
echo "Creating namespace bank-app..."
kubectl create namespace bank-app 2>/dev/null || true

# Create database secrets
echo "Creating database secrets..."
kubectl create secret generic bank-db-secret \
  --from-literal=user-password='YOUR_STRONG_USER_PASSWORD' \
  --from-literal=postgres-password='YOUR_STRONG_POSTGRES_PASSWORD' \
  --namespace bank-app

# Create Keycloak secrets
echo "Creating Keycloak secrets..."
kubectl create secret generic keycloak-secret \
  --from-literal=admin-password='YOUR_STRONG_ADMIN_PASSWORD' \
  --from-literal=database-password='YOUR_STRONG_KEYCLOAK_DB_PASSWORD' \
  --namespace bank-app

# Create microservice secrets
echo "Creating microservice secrets..."
kubectl create secret generic accounts-service-secret \
  --from-literal=database-password='YOUR_STRONG_SERVICE_PASSWORD' \
  --namespace bank-app

kubectl create secret generic cash-service-secret \
  --from-literal=database-password='YOUR_STRONG_SERVICE_PASSWORD' \
  --namespace bank-app

kubectl create secret generic transfer-service-secret \
  --from-literal=database-password='YOUR_STRONG_SERVICE_PASSWORD' \
  --namespace bank-app

kubectl create secret generic notifications-service-secret \
  --from-literal=database-password='YOUR_STRONG_SERVICE_PASSWORD' \
  --namespace bank-app

echo "All secrets created successfully!"
echo "IMPORTANT: Remember to replace the placeholder values with actual strong passwords."

echo "Listing created secrets:"
kubectl get secrets -n bank-app

echo ""
echo "To view secret details, use:"
echo "kubectl get secret <secret-name> -n bank-app -o yaml"