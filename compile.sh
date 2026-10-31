#!/bin/sh
rm -rf out && mkdir out
javac -d out src/drone/*.java && cp -r src/web out/web && jar cfe DroneFleet.jar drone.Main -C out . && echo "Built DroneFleet.jar"
