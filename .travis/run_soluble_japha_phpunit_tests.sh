#!/usr/bin/env bash
#
# Travis specific post-test script to run the test suite provided
# in the latest release of https://github.com/belgattitude/soluble-japha client.
#
#
# usage:
#   > ./run_soluble_japha_phpunit_tests.sh
#
# requirements:
#   - php >= 5.6, php 7+
#   - git
#   - composer
#   - linux
#
# @author Vanvelthem Sébastien
#

set -e

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_DIR="$SCRIPT_DIR/.."
JAPHA_DIR="$SCRIPT_DIR/soluble-japha"


clean_soluble_japha_latest() {


    # 1. Clone the soluble-japha project (if not already exists)
    if [ -d $JAPHA_DIR ]; then
        echo "[*] Clean soluble-japha";
        rm -rf $JAPHA_DIR
    fi
}


install_soluble_japha_latest() {

    echo "[*] Installing master branch of soluble-japha";

    # 1. Clone the soluble-japha project (if not already exists)
    if [ ! -d $JAPHA_DIR ]; then
        git clone https://github.com/belgattitude/soluble-japha.git $JAPHA_DIR
    fi

    # 2. Clone the soluble-japha project
    cd $JAPHA_DIR

    # 3. Checkout latest release
    #git fetch --tags
    #latestTag=$(git describe --tags `git rev-list --tags --max-count=1`)
    #git checkout ${latestTag}

    # Travis does not support php 7.1 for java image
    # so let's get the lates php5.6 release to test
    #git checkout master

    git checkout tags/1.4.5 -b travis_test

    # 4. Run composer install
    composer update

    # 5. Restore path
    cd $PROJECT_DIR
}

launchTomcatRun() {
    echo "[*] Launching tomcatRun";
    cd $PROJECT_DIR
    ./gradlew clean tomcatRun
    netstat -nlp | grep :8093
}

stopTomcat() {
    echo "[*] Stopping tomcat";
    cd $PROJECT_DIR
    ./gradlew tomcatStop
}


runPHPUnit()  {
    cd $JAPHA_DIR
    echo "[*] Running phpunit"
    cp ../phpunit.travis.xml .
    ./vendor/bin/phpunit -c ./phpunit.travis.xml -v
    if [ "$?" -ne "0" ]; then
      exit 1;
    fi
    exit 0;
}



# Here's the steps
clean_soluble_japha_latest;
install_soluble_japha_latest;
launchTomcatRun;
runPHPUnit;
#stopTomcat;

