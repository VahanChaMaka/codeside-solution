cd ../
START ./local-runner/aicup2019.exe --config "config.json"
cd ./codeside-solution
START /WAIT CMD /c mvn clean install
java -jar ./target/codeside-solution-jar-with-dependencies.jar "127.0.0.1" "31001" "0000000000000000" "local"