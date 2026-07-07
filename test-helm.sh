#!/bin/bash

# Script to test Helm charts

echo "Testing Helm charts..."

# Test the chart
echo "Running Helm lint..."
helm lint helm-charts/bank-app-chart

# Dry run to check template rendering
echo "Running Helm dry-run..."
helm install bank-app-test helm-charts/bank-app-chart \
  --dry-run \
  --namespace bank-app-test \
  --create-namespace

# Run Helm tests
echo "Running Helm tests..."
helm test bank-app-test --namespace bank-app-test

# Clean up
echo "Cleaning up test release..."
helm uninstall bank-app-test --namespace bank-app-test

echo "Helm chart tests completed!"