# Activity-Tracking-App
Activity Tracking Application for runnersusing Map-Reduce framework ,for Distributed Systems course @ AUEB 

## About 
The purpose of this project was to create a distributed activity tracking application (similar to Strava or MapMyRun) implemented in a map-reduce server model. Every client connects to the main server where he can upload his GPX files and then the files are distributed to workers for processing. 

An image explaining exactly the Map-Reduce framweork in our application:

<img width="671" alt="map-reduce" src="https://github.com/mkontaratou/Activity-Tracking-App/assets/76455116/363d4d5b-8e86-4d25-a830-640b43aea612">

In the backend, the application includes techniques such as synchronization and multithreading (using TCP sockets) so that the system is able to handle multiple requests at the same time. As far as the frontend is concerned, the communication between the server and the application also utilizes TCP sockets and the goal was to create an app friendly for the user. 

## How to run 
For the program to run successdully, you need to open the Backend folder on Intellij IDEA and run the following with the exact order:
1. Master.java
2. Worker.java
3. User.java

Afterwards, you need to open Android Studio and run the application using the correct IP ports.

## Demo 
https://github.com/mkontaratou/Activity-Tracking-App/assets/76455116/12e81665-6024-418e-9048-eaccd2ad391b


## Contributors
[Evangelos Venetsanos](https://github.com/Evanven) <br />
[Maria Kontaratou](https://github.com/mkontaratou) <br />
[Marianna Mouzouraki](https://github.com/mariannamouz) <br />
