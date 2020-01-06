#!/bin/bash

cd ../../
mvn clean install -Pdistribute
cd etc/ci/

#git clone https://github.com/PioBeat/bigraphs.git
#cp -a ../../target/site/. ./bigraphs/apidocs/
#git commit -m "updated github pages"
#git push origin gh-pages
#rm -rf ./bigraphs

#git remote set-url origin git@github.com:<Username>/<Project>.git