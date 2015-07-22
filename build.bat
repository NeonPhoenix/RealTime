cd build\libs
del /Q *
cd ..\..
gradlew.bat build --refresh-dependencies
cd build\libs
copy *universal.jar ..\..\Releases\1.7.10\10.13.4.1490\1.1.1\
pause