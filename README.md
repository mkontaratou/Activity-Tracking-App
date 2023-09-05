# Activity-Tracking-App


## About 
The purpose of this project was to create a distributed activity tracking application (similar to Strava or MapMyRun) implemented in a map-reduce server model. Every client connects to the main server where he can upload his GPX files. The files are then distributed to different workers for processing and then the results are given back to each user. 

An visual representation of the Map-Reduce framweork in our application:

<img width="671" alt="map-reduce" src="https://github.com/mkontaratou/Activity-Tracking-App/assets/76455116/4aca7c7a-4a0d-4898-8b6d-59b557445317">


In the backend, the application includes techniques such as synchronization and multithreading (using TCP sockets) so that the system is able to handle multiple requests at the same time. As far as the frontend is concerned, the communication between the server and the application also utilizes TCP sockets. The goal was to create a consistent and user-friendly application 

## How to run 
For the program to run successdully, you need to open the Backend folder on Intellij IDEA and run the following with the exact order:
1. Master.java
2. Worker.java
3. User.java

Afterwards, you need to open Android Studio and run the application using the correct IP ports.

## Demo 

https://github.com/mkontaratou/Activity-Tracking-App/assets/76455116/2ebcd75f-7e29-4938-a5a6-eafa6c1ba3a1


## Contributors
[Evangelos Venetsanos](https://github.com/Evanven) <br />
[Maria Kontaratou](https://github.com/mkontaratou) <br />
[Marianna Mouzouraki](https://github.com/mariannamouz) <br />
