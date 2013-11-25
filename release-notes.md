# Release notes

## Release 5.0.0
- Added web admin interface to Hub
- Using improved Webbit web sockets library
- Added support for Arduino Mega and other Arduinos
- Improved interface of Hub, including showing serial device names
- Merged core plug-ins with main Hub application
- Added open source licensing terms
- Changed Hub command /service/core/ to /service/tools/
- Added generic serial service
- Miscellaneous fixes and improvements

## Release 5.0.1
- Fixed issue 14: Serial connect message wrong on PC - returns //./COM3 instead of COM3
- Fixed issue 15: Serial lock fix on Mac does not always work
- Fixed issue 16: Listening on the serial service does not work
- Fixed issue 17: Serial connect on PC returns improper device string 
- Fixed issue 18: XBee is not working 
- Fixed issue 19: Restart on PC frequently not possible due to port 51000 in use 
- Fixed issue 20: If serial port is previously used as an Arduino connection, can't use as Serial
- Fixed issue 22: Connect OK message sent too soon 
- Fixed issue 23: Call to Firmata pinMode every time command is sent
- Added custom log4j properties file to make XBee API logging less verbose.
- Miscellaneous improvements to support unit testing
- Miscellaneous code cleanup and refactorings