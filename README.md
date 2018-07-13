# OpenComputersRobots
A Java TCP Server and lua scripts for controlling opencomputers robots to mine/move around

Tested with opencomputers version 1.7.2.67 for minecraft 1.12.2 and java 8

Robots open TCP sockets to Java server, server coordinates their movement and tasks 

To set up
  Each robot needs to have an eeprom with the file 'res/bios.lua'. Edit that file to point at your server in the function "request".
  In order to point at localhost, you need to edit a config file and remove it from the opencomputers blacklist (it's an opencomputers config file)(
  the files controller/api/tablet_connect.lua and robot/rom.lua 
  
  Start the server
  
  Actually, this isn't implemented yet. Hold on
