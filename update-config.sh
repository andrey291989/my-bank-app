#!/bin/bash

# Script to update application configuration in Kubernetes

echo "Updating application configuration..."

# Upgrade the Helm release with new configuration
echo "Upgrading Helm release with new configuration..."
helm upgrade bank-app helm-charts/bank-app-chart \
  --namespace bank-app \
  --reuse-values \
  "$@"

echo "Configuration updated successfully!"

echo "Checking status of pods..."
kubectl get pods -n bank-app

echo "To view detailed status, run:"
echo "kubectl get pods -n bank-app -w"