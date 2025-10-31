#!/bin/bash
# Set Java 21 for this project only

# First, check if Java 21 is installed
if [ ! -d "/opt/homebrew/opt/openjdk@21" ]; then
    echo "Java 21 not found. Installing..."
    brew install openjdk@21
fi

# Set Java 21 environment
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH

echo "Using Java 21 for this project:"
java -version

echo ""
echo "Starting backend with Java 21..."
cd backend
mvn spring-boot:run
