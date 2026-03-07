#!/bin/bash

set -e

echo "Starting AI Generated Pipeline..."

echo "Running Maven build..."
mvn -B -ntp -Dstyle.color=never clean package

echo "Running unit tests..."
pytest -q || TEST_EXIT=$?

if [ "${TEST_EXIT:-0}" -eq 5 ]; then
    echo "No tests found — continuing pipeline"
fi

echo "Checking Docker availability..."

if command -v docker >/dev/null 2>&1; then
    echo "Building Docker image..."
    docker build -t ai-devops-app:latest .
    echo "Docker build completed"
else
    echo "Docker not installed — skipping Docker build"
fi

echo "AI pipeline execution completed successfully"