**Project Title**\
Real-Time Group Chat

**Description**\
This is Java-based real-time group chat application built using Swing for the user interface. It enables several users 
to join a server and communication in a clean, modern environment.

**Features**
* Real-time messaging between multiple users
* Bubble-style chat UI with left/right alignment
* Emoji picker with popup panel
* Message reactions (click on any message to react
* Avatar initials for each user
* "Users Online" panel that updates in real-time as users join
* Portable .jar packaging for each sharing and running

**How to run**\
1. Start the server:\
```java -jar ChatServer.jar```\
You should see:\
Server started. Waiting for clients...


2. Start the Server\
```java -jar ChatApp.jar```\
Enter a username when prompted\
Enter the server IP (use localhost if the server is on the same machine)


3. You are now in the group chat!\
Enjoy and communicate with your friends when they join in using the same instructions.

**Tech stack**
* Java - Core programming language
* Java Swing - GUI framework for building the desktop interface
* Java Sockets (java.net.Socket) - Real-time client-server communication
* Multithreading - Handles multiple clients concurrently on the server
* JAR Packaging - Distributes the application as portable executable files
* HTML in Swing - Renders styled message content (emoji support, bubbles)

**Contribution guide**\
Saad Siddiqui: Client GUI Designer
* Designed and implement the Java Swing interface
* Created the chat layout with bubble-style messages
* Style the input bar, scroll pane, and integrated the avatar panel
* Ensured responsive layout and user experience consistency

Zain Naqvi: Server-Side Engineer
* Developed the multi-threaded server using Socket and ServerSocket
* Handled client connection management and JOIN/LEAVE events
* Broadcast messages and maintained the list of active users
* Ensured clean shutdowns and stable message delivery

Shem Jagroop: Real-Time Features Developer
* Implemented real-time message syncing between clients
* Added support for emojis in chat using Unicode and Swing fonts
* Integrated the emoji picket popup panel
* Ensured emoji messages were rendered properly across all systems

Andy Domkeu: Avatar & User Presence Designer
* Created the avatar panel with user initials
* Styled avatars as circular components
* Added the "Users Online" titled section
* Ensured dynamic updating when users joined or left

Vithuran Kankatharan: Reaction & Packaging Specialist
* Built the message reaction system (click to reach to messages)
* Handled the UI logic for attaching reactions to specific bubbles
* Packaged the project into a runnable .jar file
* Created the run instructions and user setup guide

**Screenshots**\
![img.png](img.png)
![img_1.png](img_1.png)

**License info**\
© 2025 Saad Siddiqui. All rights reserved.  
This project is for educational purposes only.