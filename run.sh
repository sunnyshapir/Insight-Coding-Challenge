#!/usr/bin/env bash

javac -classpath fansite-analytics-challenge/src/MappedPair.java fansite-analytics-challenge/src/LoginAttempt.java fansite-analytics-challenge/src/ProcessLog.java
java fansite-analytics-challenge/src/ProcessLog "fansite-analytics-challenge/log_input/log.txt" "fansite-analytics-challenge/log_output/hosts.txt" "fansite-analytics-challenge/log_output/resources.txt" "fansite-analytics-challenge/log_output/hours.txt" "fansite-analytics-challenge/log_output/blocked.txt"
