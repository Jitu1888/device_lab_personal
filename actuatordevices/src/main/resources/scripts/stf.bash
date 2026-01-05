#!/bin/bash

set +e

source ~/.bash_profile
source ~/.bashrc
source ~/.zshrc

node_version=$1
echo "the stf node_version is ${node_version}"
ip=$2
quality=$3

nvm use --delete-prefix v$node_version --silent &>/dev/null
cmd="export SCREEN_JPEG_QUALITY=$quality && stf local --cleanup false --group-timeout 36000 --public-ip $ip"
echo "node_version is $node_version" > stf.log
echo "executing cmd => $cmd" >>stf.log
eval $cmd >> stf.log 2>&1 < /dev/null &

disown
