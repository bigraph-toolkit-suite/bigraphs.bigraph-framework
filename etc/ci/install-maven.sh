#!/bin/bash
export VER="3.6.3"

echo "Installing Maven ${VER} ..."

if [ "$EUID" -ne 0 ]
  then echo "[ERROR] Please run as root"
  exit
fi

wget http://www-eu.apache.org/dist/maven/maven-3/${VER}/binaries/apache-maven-${VER}-bin.tar.gz -P /tmp
tar xvf /tmp/apache-maven-${VER}-bin.tar.gz
mkdir -p /opt/maven
mv -v /tmp/apache-maven-${VER}/* /opt/maven

cat <<EOF | sudo tee /etc/profile.d/maven.sh
export MAVEN_HOME=/opt/maven
export M3_HOME=/opt/maven
export PATH=\$PATH:\$MAVEN_HOME/bin
EOF

source /etc/profile.d/maven.sh
echo $MAVEN_HOME
echo $PATH