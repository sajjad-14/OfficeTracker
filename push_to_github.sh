#!/bin/bash
# Push to GitHub Script

# User Variables
REPO_NAME="OfficeTracker"
GITHUB_USER="INSERT_USERNAME_HERE"
GITHUB_TOKEN="INSERT_TOKEN_HERE" 

# Initialize Git
git init
git add .
git commit -m "Initial commit: Self-Discipline Office Tracker App"

# Create Repo (using gh cli if available, otherwise manual instruction)
echo "--------------------------------------------------------"
echo "If you have GH CLI installed:"
echo "gh repo create $REPO_NAME --public --source=. --remote=origin"
echo "--------------------------------------------------------"
echo "Otherwise, create the repo manually on GitHub, then run:"
echo "git remote add origin https://github.com/sajjad-14/OfficeTracker.git"
echo "git branch -M main"
echo "git push -u origin main"
