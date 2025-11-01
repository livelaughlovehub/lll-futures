#!/bin/bash

# LL&L Futures Backend Startup Script with Java 21
# This script ensures the backend runs with Java 21 to avoid compilation issues

echo "🚀 Starting LL&L Futures Backend with Java 21..."

# Load .env file if it exists
if [ -f .env ]; then
    echo "📋 Loading environment variables from .env file..."
    export $(grep -v '^#' .env | xargs)
fi

# Function to find and set Java 21
setup_java21() {
    # Try to find Java 21 using java_home
    if /usr/libexec/java_home -v 21 >/dev/null 2>&1; then
        export JAVA_HOME=$(/usr/libexec/java_home -v 21)
        echo "✅ Found Java 21 via java_home: $JAVA_HOME"
    # Try Homebrew installation path
    elif [ -d "/opt/homebrew/opt/openjdk@21" ]; then
        export JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
        echo "✅ Found Java 21 via Homebrew: $JAVA_HOME"
    # Try system installation
    elif [ -d "/Library/Java/JavaVirtualMachines/openjdk-21.jdk/Contents/Home" ]; then
        export JAVA_HOME="/Library/Java/JavaVirtualMachines/openjdk-21.jdk/Contents/Home"
        echo "✅ Found Java 21 in system: $JAVA_HOME"
    else
        echo "❌ Java 21 not found. Installing via Homebrew..."
        brew install openjdk@21
        
        # Set JAVA_HOME to Homebrew installation
        export JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
        echo "✅ Installed Java 21: $JAVA_HOME"
    fi
}

# Setup Java 21
setup_java21

# Verify JAVA_HOME exists
if [ ! -d "$JAVA_HOME" ]; then
    echo "❌ JAVA_HOME directory does not exist: $JAVA_HOME"
    exit 1
fi

# Set PATH to use Java 21 first
export PATH="$JAVA_HOME/bin:$PATH"

# Verify Java version
echo "📋 Java version being used:"
java -version

echo "📋 Maven will use Java from: $JAVA_HOME"

# Kill any existing backend processes
echo "🛑 Stopping any existing backend processes..."
pkill -f "spring-boot:run" 2>/dev/null || true
pkill -f "FuturesApplication" 2>/dev/null || true

# Wait a moment for processes to stop
sleep 2

# Clean and start the backend
echo "🧹 Cleaning previous build..."
JAVA_HOME="$JAVA_HOME" mvn clean -q

echo "🔨 Starting backend with Java 21..."
echo "📋 Final verification - Maven will use:"
JAVA_HOME="$JAVA_HOME" mvn -version | head -3

echo ""
echo "ℹ️  Note: To test OAuth2 locally, set these environment variables:"
echo "   export GOOGLE_CLIENT_ID='your-client-id'"
echo "   export GOOGLE_CLIENT_SECRET='your-client-secret'"
echo ""

JAVA_HOME="$JAVA_HOME" mvn spring-boot:run -Dspring-boot.run.profiles=local
