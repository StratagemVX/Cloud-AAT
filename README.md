# Real-Time Chat Application with AI Chatbot

A full-stack real-time chat application built with **Spring Boot 3.4.3**, **WebSocket/STOMP**, and a **TF-IDF chatbot** engine — featuring a dark glassmorphism UI.

---

## Features

| Feature | Description |
|---|---|
| Real-Time Messaging | WebSocket + STOMP + SockJS for instant bi-directional communication |
| AI Chatbot | TF-IDF vectorization + cosine similarity FAQ matching |
| User Presence | Live online/offline tracking with join/leave notifications |
| Message History | Recent 50 messages loaded on connect |
| Random Question | Dice button auto-sends a random FAQ question to the chatbot |
| FAQ Panel | Slide-in panel listing all FAQ questions grouped by category |
| Dark Glassmorphism UI | Responsive design with animations and blur effects |
| H2 Database | Zero-config in-memory DB (MySQL-ready for production) |

---

## Architecture

```
Browser (SockJS + STOMP.js)
  │
  ├── /ws  ──────────────────► WebSocketConfig (STOMP broker)
  │                                  │
  │   /app/chat.sendMessage ──────► ChatController
  │   /app/chat.addUser     ──────►     ├── ChatService       ──► ChatMessageRepository
  │   /topic/public ◄──────────────     ├── ChatbotService    ──► FaqRepository
  │                                     └── UserService       ──► UserRepository
  │
  └── REST ─────────────────► UserController / ChatbotController
                                         │
                                    H2 (in-memory)
```

---

## Sequence Diagram — Chat Message Flow

```
User        SockJS/STOMP       ChatController    ChatService      H2
 │               │                   │                │            │
 │─ send msg ───►│                   │                │            │
 │               │── /app/chat.send ►│                │            │
 │               │                   │── saveMessage ►│            │
 │               │                   │                │── INSERT ──►
 │               │                   │                │◄── saved ──│
 │               │◄─── /topic/public broadcast ───────│            │
 │◄── message ───│                   │                │            │
```

---

## Sequence Diagram — Chatbot (FAQ Match)

```
User        SockJS/STOMP       ChatController    ChatbotService   FaqRepository
 │               │                   │                │                │
 │─ "message" ──►│                   │                │                │
 │               │── sendMessage ───►│                │                │
 │               │◄── user msg broadcast (FIRST) ────│                │
 │               │                   │── getResponse ►│                │
 │               │                   │              tokenize           │
 │               │                   │              TF-IDF vector      │
 │               │                   │              cosine similarity  │
 │               │                   │◄── answer ─────│                │
 │               │◄── bot reply broadcast (SECOND) ──│                │
 │◄── both msgs ─│                   │                │                │
```

**Note:** The chatbot auto-replies to **every** message. The user's message always appears **first**, followed by the bot's reply.

---

## Project Structure

```
chat-application/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/chatapp/
    │   │   ├── ChatApplication.java
    │   │   ├── config/
    │   │   │   ├── WebSocketConfig.java        # STOMP + SockJS broker config
    │   │   │   ├── WebConfig.java              # CORS + static resource config
    │   │   │   └── WebSocketEventListener.java # Disconnect handler
    │   │   ├── model/
    │   │   │   ├── User.java
    │   │   │   ├── ChatMessage.java
    │   │   │   └── FaqEntry.java
    │   │   ├── dto/
    │   │   │   ├── MessageDTO.java
    │   │   │   └── UserDTO.java
    │   │   ├── repository/
    │   │   │   ├── UserRepository.java
    │   │   │   ├── ChatMessageRepository.java
    │   │   │   └── FaqRepository.java
    │   │   ├── service/
    │   │   │   ├── ChatService.java            # Message persistence
    │   │   │   ├── UserService.java            # User lifecycle
    │   │   │   └── ChatbotService.java         # TF-IDF engine + FAQ lookup
    │   │   ├── controller/
    │   │   │   ├── ChatController.java         # WebSocket + message REST
    │   │   │   ├── UserController.java         # User REST API
    │   │   │   └── ChatbotController.java      # Chatbot REST API
    │   │   └── exception/
    │   │       └── GlobalExceptionHandler.java
    │   └── resources/
    │       ├── application.properties
    │       ├── data.sql                        # FAQ seed entries
    │       └── static/
    │           ├── index.html
    │           ├── css/style.css
    │           └── js/app.js
    └── test/
        └── java/com/chatapp/
            ├── service/  (UserServiceTest, ChatServiceTest)
            ├── controller/ (UserControllerTest)
            ├── model/    (UserTest, ChatMessageTest)
            └── dto/      (UserDTOTest, MessageDTOTest)
```

---

## How to Run in Eclipse IDE (Step-by-Step)

### Prerequisites

| Requirement | Details |
|---|---|
| **Eclipse IDE** | Eclipse IDE for Enterprise Java and Web Developers (2023-09 or newer recommended) |
| **JDK 21** | Java 21 LTS — [Download from Oracle](https://www.oracle.com/java/technologies/downloads/#java21) or [Adoptium](https://adoptium.net/) |
| **Maven** | Eclipse has the **m2e** Maven plugin built-in (no separate Maven install needed) |

### Step 1 — Install JDK 21

1. Download and install **JDK 21** from Oracle or Adoptium.
2. Note the installation path (e.g., `C:\Program Files\Java\jdk-21`).

### Step 2 — Configure JDK 21 in Eclipse

1. Open Eclipse.
2. Go to **Window → Preferences → Java → Installed JREs**.
3. Click **Add → Standard VM → Next**.
4. Browse to your JDK 21 installation directory (e.g., `C:\Program Files\Java\jdk-21`).
5. Click **Finish**, then **check the box** next to JDK 21 to make it the default.
6. Go to **Window → Preferences → Java → Compiler**.
7. Set **Compiler compliance level** to **21**.
8. Click **Apply and Close**.

### Step 3 — Import the Project into Eclipse

1. Open Eclipse.
2. Go to **File → Import → Maven → Existing Maven Projects**.
3. Click **Next**.
4. Click **Browse** and select the project root folder (the folder containing `pom.xml`).
5. Eclipse will detect the `pom.xml` — ensure it is checked.
6. Click **Finish**.
7. Wait for Eclipse to download all Maven dependencies (check the progress bar at the bottom-right).

### Step 4 — Update Maven Project

1. In the **Package Explorer**, right-click on the project name (`chat-application`).
2. Select **Maven → Update Project** (or press `Alt+F5`).
3. Check the box **Force Update of Snapshots/Releases**.
4. Click **OK**.
5. Wait for the build to complete — the console should show no errors.

### Step 5 — Run the Application

**Option A — Run as Spring Boot App (Recommended)**:
1. In **Package Explorer**, expand `src/main/java` → `com.chatapp`.
2. Right-click on `ChatApplication.java`.
3. Select **Run As → Spring Boot App**.

**Option B — Run as Java Application**:
1. Right-click on `ChatApplication.java`.
2. Select **Run As → Java Application**.

**Option C — Using Maven (from Eclipse Terminal)**:
1. Go to **Window → Show View → Terminal**.
2. Run:
   ```
   mvnw.cmd spring-boot:run
   ```

### Step 6 — Access the Application

1. Open your browser and navigate to: **http://localhost:5000**
2. Enter a username and start chatting!
3. The AI chatbot will automatically reply to every message you send.

### Step 7 — Access H2 Database Console (Optional)

1. Open your browser and navigate to: **http://localhost:5000/h2-console**
2. Enter the following connection details:

| Field | Value |
|---|---|
| JDBC URL | `jdbc:h2:mem:chatdb` |
| Username | `sa` |
| Password | *(leave empty)* |

3. Click **Connect** to browse tables (`USERS`, `CHAT_MESSAGES`, `FAQ_ENTRIES`).

### Troubleshooting in Eclipse

| Problem | Solution |
|---|---|
| **Red errors after import** | Right-click project → **Maven → Update Project** (`Alt+F5`) |
| **Java version mismatch** | Go to **Window → Preferences → Java → Installed JREs** — ensure JDK 21 is selected |
| **Build path errors** | Right-click project → **Build Path → Configure Build Path** → Libraries → set JRE to JavaSE-21 |
| **Port 5000 already in use** | Stop any running instance. Change port in `application.properties`: `server.port=5001` |
| **Dependencies not downloading** | Check internet connection. Go to **Window → Preferences → Maven** → check "Download Artifact Sources" |
| **`data.sql` not loading** | Ensure `spring.jpa.defer-datasource-initialization=true` is set in `application.properties` |

---

## How to Run via Command Line (Alternative)

**Prerequisites:** Java 21 JDK, Apache Maven 3.8+

```bash
# Build and run tests
mvn clean verify

# Start the server
mvn spring-boot:run
```

Open **http://localhost:5000** in your browser.

---

## Technology Stack

| Layer | Technology |
|---|---|
| Backend | Java 21 (LTS), Spring Boot 3.4.3 |
| WebSocket | Spring WebSocket, STOMP, SockJS |
| Database | H2 in-memory (dev) — MySQL-ready (prod) |
| ORM | Spring Data JPA / Hibernate 6.6 |
| AI / NLP | TF-IDF + Cosine Similarity (pure Java) |
| Testing | JUnit 5, Mockito 5, JaCoCo 0.8.13 |
| Frontend | HTML5, CSS3, Vanilla JavaScript |
| Client WS | SockJS 1.x, STOMP.js 2.3 |

---

## API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| WebSocket | `/ws` | SockJS + STOMP connection |
| STOMP SUB | `/topic/public` | Subscribe to broadcast |
| STOMP SEND | `/app/chat.sendMessage` | Send a chat message |
| STOMP SEND | `/app/chat.addUser` | Register user on join |
| GET | `/api/messages` | Recent message history (last 50) |
| DELETE | `/api/messages` | Clear all message history |
| GET | `/api/users/online` | List currently online users |
| POST | `/api/users/connect?username=` | Connect a user |
| POST | `/api/users/disconnect?username=` | Disconnect a user |
| POST | `/api/chatbot/ask?query=` | Direct chatbot query |
| GET | `/api/chatbot/random-question` | Return a random FAQ question |
| GET | `/api/chatbot/questions` | Return all FAQ questions by category |

---

## Chatbot

The chatbot uses a **TF-IDF + cosine similarity** engine. It auto-replies to **every message** you send. The user's message always appears **first** in the chat, followed by the bot's reply.

**Similarity threshold:** 0.15 — queries scoring below this return a fallback response.

---

## Chatbot FAQ — Full List of Questions You Can Ask

Below is the complete list of questions the chatbot can answer, organized by category. You can type any of these (or similar variations) in the chat to get a response.

### 💬 General

| # | Question |
|---|---|
| 1 | What is this application |
| 2 | Who created this application |
| 3 | What is the purpose of this chat app |
| 4 | How does this chat application work |
| 5 | Tell me about this project |

### ✨ Features

| # | Question |
|---|---|
| 6 | What features does this application have |
| 7 | What features does this application provide |
| 8 | How does the FAQ panel work |
| 9 | How does the random question feature work |
| 10 | Can multiple users chat at the same time |
| 11 | How many messages are stored in message history |
| 12 | What is the glassmorphism UI design |

### 🤖 Chatbot

| # | Question |
|---|---|
| 13 | How does the chatbot work |
| 14 | How to use the chatbot |
| 15 | What is TF-IDF |
| 16 | What is cosine similarity |
| 17 | Do I need to use the @bot prefix |
| 18 | What happens when the chatbot does not find an answer |
| 19 | How many FAQ entries does the chatbot know |

### ⚙️ Technology

| # | Question |
|---|---|
| 20 | What technology stack is used |
| 21 | What is WebSocket |
| 22 | How does WebSocket messaging work |
| 23 | What is STOMP protocol |
| 24 | What is SockJS |
| 25 | What database is used |
| 26 | What is Spring Boot |
| 27 | What Java version does this application use |
| 28 | Does this application have unit tests |
| 29 | What is Spring Data JPA |
| 30 | What is Hibernate |
| 31 | How do I access the H2 database console |
| 32 | How do I switch to MySQL for production |

### 🏗️ Architecture

| # | Question |
|---|---|
| 33 | Explain the architecture |
| 34 | How does real-time messaging work |
| 35 | How does user presence tracking work |

### 📖 Usage

| # | Question |
|---|---|
| 36 | How to run this application |
| 37 | How to send a message |
| 38 | Can I see who is online |
| 39 | What port does the application run on |
| 40 | What happens when I disconnect or leave chat |
| 41 | Is there user authentication or login |
| 42 | What does the clear history button do |

> **Tip:** You can also click the **❓ FAQ button** (top-right of the chat header) to see all questions in a slide-in panel, or click the **🎲 Dice button** (bottom-left of the message input) to send a random question to the bot.

---

## Java 21 Upgrade

This project was upgraded from **Java 17 to Java 21 LTS** (branch `appmod/java-upgrade-20260307172059`).

| Item | Before | After |
|---|---|---|
| Java | 17 | **21 (LTS)** |
| Spring Boot | 3.2.5 | **3.4.3** |
| JaCoCo | — | **0.8.13** |
| Mockito | 5.7.0 (via BOM) | **5.11+ (via 3.4.3 BOM)** |
| Eclipse JRE container | JavaSE-19 | **JavaSE-21** |
| Eclipse compiler compliance | 19 | **21** |

**Key changes made:**

- `pom.xml` — `java.version` set to `21`; Spring Boot upgraded to `3.4.3`; JaCoCo upgraded to `0.8.13`; Surefire configured with `--add-opens` flags for Java 21+ module access
- `.classpath` — JRE container updated to `JavaSE-21`
- `.settings/org.eclipse.jdt.core.prefs` — compiler compliance, source, and target set to `21`
- `src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker` — set to `mock-maker-subclass` for compatibility with Java 21+ sealed `java.base` module

**Test results after upgrade:** 37/37 tests pass, BUILD SUCCESS.

---

## License

This project is for educational and demonstration purposes.
