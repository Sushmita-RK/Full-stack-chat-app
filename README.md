# Java + React Full-Stack Chat Application

![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)
![Spring](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?logo=spring&logoColor=white)
![React](https://img.shields.io/badge/React-18.x-61DAFB?logo=react&logoColor=black)
![Vite](https://img.shields.io/badge/Vite-B73BFE?logo=vite&logoColor=white)
![WebSocket](https://img.shields.io/badge/WebSocket-black?logo=websocket&logoColor=white)

This is a real-time chat application built using Spring Boot for the backend and React for the frontend, featuring user authentication, public chat, and private 1-to-1 messaging.

## ✨ Features

* 🔐 **User Authentication:** Secure user registration and login using JWT (JSON Web Tokens).
* ⚡ **Real-time Messaging:** Uses WebSockets (with STOMP and SockJS) for instant message delivery.
* 🌍 **Public Chat Room:** All logged-in users can participate in a global chat.
* 👤 **Private 1-to-1 Chat:** Users can select other online users for private conversations.
* 📋 **Dynamic User List:** Fetches and displays a list of registered users (excluding the current user).

## 💻 Tech Stack

| Backend (Java - Spring Boot) | Frontend (React) |
| :--- | :--- |
| <ul><li>Java 21</li><li>Spring Boot 3.x</li><li>Spring Security (JWT)</li><li>Spring WebSocket (STOMP, SockJS)</li><li>Spring Data JPA</li><li>H2 Database (In-Memory)</li><li>Maven</li></ul> | <ul><li>React 18.x</li><li>Vite</li><li>JavaScript</li><li>CSS3</li><li>Axios</li><li>@stomp/stompjs</li><li>sockjs-client</li></ul> |

## 📂 Project Structure

java-react-chat-app/ ├── demo/ # Spring Boot Backend └── chat-frontend/ # React Frontend


## 🚀 Getting Started

### Prerequisites

* Java JDK 21 or higher
* Node.js (v18 or higher) and npm
* Git

### 1. Clone the Repository

git clone [https://github.com/Tarun72432/My-first-full-stack-chat-app.git](https://github.com/Tarun72432/My-first-full-stack-chat-app.git)
cd My-first-full-stack-chat-app
2. Run the Backend (Server 1)
Navigate to the backend directory:

cd demo
Run the Spring Boot application using Maven Wrapper:

(This will automatically download dependencies)
# For macOS/Linux
./mvnw spring-boot:run

# For Windows (CMD)
mvnw.cmd spring-boot:run
The backend server will start on http://localhost:8080.

3. Run the Frontend (Server 2)
Open a new terminal and navigate to the frontend directory:

# If you are in the 'demo' folder
cd ../chat-frontend

# If you are in the root 'java-react-chat-app' folder
cd chat-frontend
Install dependencies:

npm install
Run the React development server:

npm run dev
The frontend application will open in your browser at http://localhost:5173.

4. Access the App
Open http://localhost:5173 in your web browser.

Register a new user or log in if you already have an account.

To test private chat: Register and log in with at least two different users in separate browser tabs or private windows.
