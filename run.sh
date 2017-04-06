#!/usr/bin/env bash

javac ./src/ProcessLog.java ./src/MappedPair.java ./src/LoginAttempt.java 
java -cp ./src/ ProcessLog "./log_input/log.txt" "./log_output/hosts.txt" "./log_output/resources.txt" "./log_output/hours.txt" "./log_output/blocked.txt"
