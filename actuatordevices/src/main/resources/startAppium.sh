#!/bin/sh"

count=$(lsof -i -P -n | grep LISTEN | grep 4723 | wc -l)

if [ $# -eq 0 ]
  then
    echo "No arguments supplied, please pass in the absolute path of serverconfig.json"
elif [ $count -eq 0 ]
  then
    source ~/.bash_profile
    source ~/.zshrc
    rm -rf  /tmp/appium_backup.log
   	mv /tmp/appium.log /tmp/appium_backup.log
    echo "appium path --- $(which appium)" >> /tmp/appium.log
    echo "$(date -u) - Starting Appium server during actuator devices" >> /tmp/appium.log
    echo "CLOUD_USERNAME='username' CLOUD_KEY='apiKey' appium --local-timezone --log-timestamp --allow-insecure chromedriver_autodownload --config $1" >> /tmp/appium.log
    CLOUD_USERNAME='username' CLOUD_KEY='apiKey' appium server --log-timestamp --allow-insecure chromedriver_autodownload --config $1 >> /tmp/appium.log  2>&1 &
    echo "$(date -u) - processID is $!" >> /tmp/appium.log
    echo "$(date -u) - Appium server started" >> /tmp/appium.log
else
    echo "Appium is already running on port 4723" >> /tmp/appium.log
fi