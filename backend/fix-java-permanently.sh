#!/bin/bash

# Permanent Java 21 Setup for LL&L Futures
# This script configures your system to use Java 21 by default for Maven

echo "🔧 Setting up Java 21 as default for development..."

# Backup existing profile
if [ -f ~/.zshrc ]; then
    cp ~/.zshrc ~/.zshrc.backup.$(date +%Y%m%d_%H%M%S)
    echo "✅ Backed up existing .zshrc"
fi

# Check if Java 21 is installed
if ! /usr/libexec/java_home -v 21 >/dev/null 2>&1; then
    echo "📦 Installing Java 21 via Homebrew..."
    brew install openjdk@21
fi

# Add Java 21 configuration to .zshrc
echo "📝 Adding Java 21 configuration to ~/.zshrc..."

cat >> ~/.zshrc << 'EOF'

# LL&L Futures Java 21 Configuration
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"

# Alias for quick Java version switching
alias java21='export JAVA_HOME=$(/usr/libexec/java_home -v 21) && export PATH="$JAVA_HOME/bin:$PATH"'
alias java22='export JAVA_HOME=$(/usr/libexec/java_home -v 22) && export PATH="$JAVA_HOME/bin:$PATH"'

EOF

echo "✅ Configuration added to ~/.zshrc"
echo "🔄 Please run 'source ~/.zshrc' or restart your terminal"
echo "🎯 After that, 'mvn spring-boot:run' should work without issues"

# Show current Java versions
echo "📋 Available Java versions:"
/usr/libexec/java_home -V
