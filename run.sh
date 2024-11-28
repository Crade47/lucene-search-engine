#!/bin/bash

# Checking the setup status
# Function to check if a package is installed
is_installed() {
    dpkg -l | grep -qw "$1"
}

# Ensure the script is run as root
if [ "$EUID" -ne 0 ]; then
    echo "Please run this script as root or with sudo"
    exit 1
fi

# List of required packages
PACKAGES=("openjdk-11-jdk" "curl" "tar")

# Install missing packages
for pkg in "${PACKAGES[@]}"; do
    if ! is_installed "$pkg"; then
        echo "Installing $pkg..."
        apt-get update && apt-get install -y "$pkg"
    else
        echo "$pkg is already installed."
    fi
done

# Clean up the apt cache
apt-get clean

# Maven installation variables
MAVEN_VERSION="3.8.8"
MAVEN_BASE_URL="https://downloads.apache.org/maven/maven-3"
MAVEN_TAR="apache-maven-${MAVEN_VERSION}-bin.tar.gz"
MAVEN_INSTALL_DIR="/opt/apache-maven-${MAVEN_VERSION}"
MAVEN_SYMLINK="/usr/bin/mvn"

# Check if Maven is already installed
if [ ! -d "$MAVEN_INSTALL_DIR" ]; then
    echo "Downloading and installing Maven $MAVEN_VERSION..."
    curl -fsSL "${MAVEN_BASE_URL}/${MAVEN_VERSION}/binaries/${MAVEN_TAR}" | tar -xz -C /opt
    ln -s "${MAVEN_INSTALL_DIR}/bin/mvn" "$MAVEN_SYMLINK"
else
    echo "Maven $MAVEN_VERSION is already installed."
fi


cd  twentythree
mvn clean install


echo "Setup complete."
echo "------------------Starting the application------------------"
java -jar target/twentythree-1.0-SNAPSHOT.jar
echo "-----------------Finished--------------------"
