#!/bin/sh
# Web dashboard (opens browser):  sh run.sh      Desktop window: sh run.sh --swing    Console demo: sh run.sh --demo
if [ "$1" = "--swing" ]; then java -jar DroneFleet.jar; else java -jar DroneFleet.jar --web "$@"; fi
