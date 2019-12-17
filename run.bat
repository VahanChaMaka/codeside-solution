echo."%*" | findstr /C:"2x2" 1>nul
if errorlevel 1 (
	ECHO starting 2x1
	SET config="config.json"
) else (
	ECHO starting 2x2
	SET config="config2x2.json"
)
cd ../
START ./local-runner/aicup2019.exe --config %config%
START java -jar ./local-runner/simple.jar "127.0.0.1" "31002" "0000000000000000" "local"
cd ./codeside-solution
echo."%*" | findstr /C:"recompile" 1>nul
if errorlevel 1 (
	echo dont recompile
) else (
	START /WAIT CMD /c mvn clean install
)
java -jar ./target/aicup2019-jar-with-dependencies.jar "127.0.0.1" "31001" "0000000000000000" "local"