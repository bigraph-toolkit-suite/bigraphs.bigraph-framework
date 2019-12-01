#!/bin/bash
echo "Creating changelog ..."

if [ "$1" != "" ]; then
  revisionFile=$1
else
  revisionFile=../../.mvn/maven.config
fi
echo "${revisionFile}"
version=v$(cat ${revisionFile} | grep -o 'revision=.*' | cut -f2- -d'=')
if [ ${version} == "v" ]; then
  echo "ERROR: Version could not be obtained from ${revisionFile}"
  exit 1
fi

echo "[*] Version is: ${version}"

if [ "$2" != "" ]; then
  filename=$1/changelog_${version}.txt
else
  filename=./changelog_${version}.txt
fi
echo "[*] Output path set to=${filename}"
echo "[*] Writing changelog now ..."

echo "# Changelog - ${version} [$(date)]" >${filename}

echo "## Added" >>${filename}
git log --oneline --pretty=format:'<li> <a href="commit/%H">view commit &bull;</a> %s</li> ' --reverse | grep ^added: >>${filename}

echo "## Changed" >>${filename}
git log --oneline --pretty=format:'<li> <a href="commit/%H">view commit &bull;</a> %s</li> ' --reverse | grep ^changed: >>${filename}

echo "## Removed" >>${filename}
git log --oneline --pretty=format:'<li> <a href="commit/%H">view commit &bull;</a> %s</li> ' --reverse | grep ^removed: >>${filename}

echo "## Bugfix" >>${filename}
git log --oneline --pretty=format:'<li> <a href="commit/%H">view commit &bull;</a> %s</li> ' --reverse | grep ^bugfix: >>${filename}

echo "[*] Writing changelog now ... finished"