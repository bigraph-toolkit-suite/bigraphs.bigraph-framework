#!/bin/bash
echo "Creating changelog ..."

baseuriGit=https://git-st.inf.tu-dresden.de/bigraphs/bigraph-framework/commit/

getGitLogsFor() {
  formatOutput="- %s [view commit &#x2197;](${baseuriGit}%H)"
  local result=$(git log --oneline --pretty=format:"${formatOutput}" --reverse | grep $1)
  replaceString="- "
  result=$(echo "$result" | sed "s/- - /$replaceString/")
  echo "${result}"
  return 0
}

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
  filename=$1/changelog_${version}.md
else
  filename=./changelog_${version}.md
fi

echo "[*] Output path set to=${filename}"
echo "[*] Writing changelog now ..."

echo "## [${version}] - $(date '+%F')" >${filename}

#git log v0.6.0-SNAPSHOT --oneline --pretty=format:'<li> <a href="commit/%H">view commit &bull;</a> %s</li> ' --reverse | grep ^added: >>${filename}

echo "### Added" >>${filename}
echo "$(getGitLogsFor 'added')" >> ${filename}

echo "### Changed" >>${filename}
echo "$(getGitLogsFor 'changed')" >> ${filename}

echo "### Removed" >>${filename}
echo "$(getGitLogsFor 'removed')" >> ${filename}

echo "### Bugfix" >>${filename}
echo "$(getGitLogsFor 'bugfix')" >> ${filename}

echo "[*] Writing changelog now ... finished"
