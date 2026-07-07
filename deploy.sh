#!/bin/bash

# Script to deploy the bank application using Helm

echo "Deploying Bank App using Helm..."

# Add bitnami repo for dependencies
echo "Adding Bitnami Helm repository..."
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update

# Build dependencies
echo "Building Helm dependencies..."
helm dependency build helm-charts/bank-app-chart

# Install or upgrade the release
echo "Installing/upgrading Helm release..."
helm upgrade --install bank-app helm-charts/bank-app-chart \
  --namespace bank-app \
  --create-namespace \
  --set front-ui.ingress.enabled=true \
  --set front-ui.ingress.hosts[0].host=bank.local \
  --set front-ui.ingress.hosts[0].paths[0].path=/ \
  --set front-ui.ingress.hosts[0].paths[0].pathType=Prefix \
  --set gateway.ingress.enabled=true \
  --set gateway.ingress.hosts[0].host=api.bank.local \
  --set gateway.ingress.hosts[0].paths[0].path=/api \
  --set gateway.ingress.hosts[0].paths[0].pathType=Prefix

echo "Bank App deployed successfully!"
echo "To access the application, add the following entries to your /etc/hosts file:"
echo "127.0.0.1 bank.local"
echo "127.0.0.1 api.bank.local"