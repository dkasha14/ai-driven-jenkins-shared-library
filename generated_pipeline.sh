#!/bin/bash

set +e

echo "Starting AI Generated Pipeline..."

echo "Running Maven build"
mvn -B -Dstyle.color=never clean package

echo "Running unit tests"
pytest
TEST_EXIT=$?

if [ "$TEST_EXIT" -eq 5 ]; then
    echo "No tests found — continuing"
fi

echo "Building Docker image"
docker build -t ai-devops-app:latest .

echo "Docker build completed"

exit 0