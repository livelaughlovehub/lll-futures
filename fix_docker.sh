#!/bin/bash
# Fix Docker Maven version and push to GitHub

echo "Fixing Docker Maven version..."

# Navigate to backend directory
cd backend

# Check current Dockerfile content
echo "Current Dockerfile content:"
head -3 Dockerfile

# Update Dockerfile if needed
if grep -q "maven:3.9.5-openjdk-17-slim" Dockerfile; then
    echo "Updating Maven version in Dockerfile..."
    sed -i '' 's/maven:3.9.5-openjdk-17-slim/maven:3.8.6-openjdk-17-slim/' Dockerfile
    echo "Updated!"
else
    echo "Dockerfile already has correct version"
fi

# Check updated content
echo "Updated Dockerfile content:"
head -3 Dockerfile

# Go back to root
cd ..

# Add, commit, and push
echo "Committing and pushing changes..."
git add .
git commit -m "Fix Docker Maven image version - use 3.8.6"
git push origin main

echo "Done! Check your GitHub repository to confirm the changes."
