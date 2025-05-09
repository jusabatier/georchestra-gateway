#!/bin/bash

# This script sets up MkDocs for the geOrchestra Gateway documentation

# Exit on error
set -e

echo "Checking Python and Python version"

# Try to find a suitable Python executable
PYTHON_BIN=""

# Function to compare Python versions
version_ge() {
  # Compare two versions, returns 0 if $1 >= $2
  [ "$(printf '%s\n' "$1" "$2" | sort -V | head -n1)" = "$2" ]
}

# Check for python or python3
for cmd in python python3; do
  if command -v $cmd >/dev/null 2>&1; then
    VERSION=$($cmd -c 'import sys; print(".".join(map(str, sys.version_info[:3])))')
    if version_ge "$VERSION" "3.11"; then
      PYTHON_BIN=$cmd
      break
    fi
  fi
done

if [ -z "$PYTHON_BIN" ]; then
  echo "Python 3.11+ not found. Please install a suitable version."
  exit 1
fi

# Python found !
echo "Using $PYTHON_BIN (version $VERSION)"

# Create the virtual environment
$PYTHON_BIN -m venv venv_mkdocs
echo "Virtual environment created in ./venv_mkdocs/"

# Activate the venv
if [ -n "$WINDIR" ] || [ -n "$MSYSTEM" ]; then
    echo "Sorry : you are on Windows"
    source venv_mkdocs/Scripts/activate
else
    source venv_mkdocs/bin/activate
fi

# Install MkDocs and required plugins
echo "Installing MkDocs and plugins..."
python -m pip install -r mkdocs_requirements.txt
python -m pip list

echo ""
echo "======================================================================="
echo ""
echo ""

# Check if MkDocs was installed successfully
if ! mkdocs --version &> /dev/null; then
    echo "Error: MkDocs installation failed"
    exit 1
fi

echo "MkDocs installed successfully"

# Check if docs directory structure exists
if [ ! -d "docs/user_guide" ] || [ ! -d "docs/arc42" ] || [ ! -d "docs/developer_guide" ]; then
    echo "Creating docs directory structure..."
    mkdir -p docs/user_guide docs/arc42 docs/developer_guide
fi

# Create missing placeholder files
touch_if_not_exists() {
    if [ ! -f "docs/$1" ]; then
        echo "Creating placeholder: docs/$1"
        echo "# $2" > "docs/$1"
        echo "" >> "docs/$1"
        echo "This page is under construction." >> "docs/$1"
    fi
}

# User Guide
touch_if_not_exists "user_guide/installation.md" "Installation"
touch_if_not_exists "user_guide/configuration.md" "Configuration"
touch_if_not_exists "user_guide/access_rules.md" "Access Rules"
touch_if_not_exists "user_guide/headers.md" "Headers"
touch_if_not_exists "user_guide/ui_customization.md" "UI Customization"

# Arc42
touch_if_not_exists "arc42/architecture_goals.md" "Architecture Goals"
touch_if_not_exists "arc42/context_view.md" "Context View"
touch_if_not_exists "arc42/container_view.md" "Container View"
touch_if_not_exists "arc42/runtime_view.md" "Runtime View"
touch_if_not_exists "arc42/deployment_view.md" "Deployment View"
touch_if_not_exists "arc42/crosscutting.md" "Crosscutting Concerns"
touch_if_not_exists "arc42/quality_requirements.md" "Quality Requirements"
touch_if_not_exists "arc42/risks.md" "Risks and Technical Debt"
touch_if_not_exists "arc42/glossary.md" "Glossary"

# Developer Guide
touch_if_not_exists "developer_guide/code_style.md" "Code Style"
touch_if_not_exists "developer_guide/project_structure.md" "Project Structure"
touch_if_not_exists "developer_guide/testing.md" "Testing"
touch_if_not_exists "developer_guide/authentication.md" "Authentication"
touch_if_not_exists "developer_guide/contributing.md" "Contributing"

echo "MkDocs setup complete. To serve the documentation, run:"
echo "./mkdocs_run.sh"