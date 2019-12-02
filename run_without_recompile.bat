cd ../
START ./local-runner/aicup2019.exe --config "config.json"
cd ./codeside-solution
java -jar ./target/aicup2019-jar-with-dependencies.jar "127.0.0.1" "31001" "0000000000000000" "local"