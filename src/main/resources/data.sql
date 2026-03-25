-- ═══════════════════════════════════════════════════════════════════════
--  SEED DATA — FAQ Entries for the TF-IDF Chatbot
--
--  Each entry includes:
--    question  — the main reference question
--    answer    — the response returned by the chatbot
--    category  — organizational grouping
--    keywords  — comma-separated synonyms/aliases that enrich TF-IDF
--                matching so paraphrased queries still find a match
--
--  The ChatbotService combines (question + keywords) when building
--  TF-IDF vectors, giving much better matching tolerance.
-- ═══════════════════════════════════════════════════════════════════════

-- ── General / About ────────────────────────────────────────────────────
INSERT INTO faq_entries (question, answer, category, keywords) VALUES
('What is this application',
 'This is a full-stack real-time chat application built with Spring Boot, WebSocket, STOMP, and SockJS. It features instant messaging, user presence tracking, and an AI-powered chatbot using TF-IDF similarity.',
 'general',
 'app,application,chat,about,describe,tell,overview,explain,introduction,purpose,project,system');

INSERT INTO faq_entries (question, answer, category, keywords) VALUES
('Who created this application',
 'This chat application was developed as a full-stack project demonstrating real-time communication, AI chatbot integration, and modern web technologies.',
 'general',
 'creator,author,developer,made,built,who,created,designed,developed');

INSERT INTO faq_entries (question, answer, category, keywords) VALUES
('What is the purpose of this chat app',
 'The purpose is to provide a real-time communication platform where users can send messages instantly, see who is online, and interact with an intelligent chatbot for quick answers to frequently asked questions.',
 'general',
 'purpose,goal,aim,objective,reason,why,motivation,intent,use,useful');

-- ── Features ───────────────────────────────────────────────────────────
INSERT INTO faq_entries (question, answer, category, keywords) VALUES
('What features does this application have',
 'Key features include: real-time messaging via WebSocket, user presence tracking (online/offline), join and leave notifications, AI chatbot with TF-IDF intent matching, message history, and a responsive dark glassmorphism UI.',
 'features',
 'features,functionality,capabilities,functions,options,provide,offer,support,list,have,app,does,do');

INSERT INTO faq_entries (question, answer, category, keywords) VALUES
('What features does this application provide',
 'This application supports real-time chat using WebSockets, user presence tracking, message history storage, and an AI chatbot for answering frequently asked questions.',
 'features',
 'features,functionality,capabilities,what,can,app,do,provide,support,offer,have');

-- ── Chatbot ────────────────────────────────────────────────────────────
INSERT INTO faq_entries (question, answer, category, keywords) VALUES
('How does the chatbot work',
 'The chatbot uses TF-IDF (Term Frequency-Inverse Document Frequency) vectorization and cosine similarity to match your question against a database of FAQs. It tokenizes your query, removes stop-words, computes a TF-IDF vector, and compares it against pre-computed FAQ vectors. If the similarity score exceeds 0.15, it returns the best matching answer; otherwise it gives a fallback response.',
 'chatbot',
 'chatbot,bot,ai,work,function,how,mechanism,logic,algorithm,tfidf,matching,process,nlp');

INSERT INTO faq_entries (question, answer, category, keywords) VALUES
('How to use the chatbot',
 'To interact with the chatbot, simply type your message starting with @bot followed by your question. For example: @bot what features does this app have? The bot will analyze your question and find the most relevant answer from its FAQ database.',
 'chatbot',
 'chatbot,bot,use,usage,interact,command,prefix,trigger,talk,ask,query,help');

INSERT INTO faq_entries (question, answer, category, keywords) VALUES
('What is TF-IDF',
 'TF-IDF stands for Term Frequency-Inverse Document Frequency. It is a numerical statistic that reflects how important a word is to a document in a collection. TF measures how often a term appears in a document, while IDF measures how rare the term is across all documents. Together they create weighted vectors used for text similarity comparison.',
 'chatbot',
 'tfidf,tf,idf,term,frequency,inverse,document,algorithm,weight,statistic,nlp,text,analysis');

INSERT INTO faq_entries (question, answer, category, keywords) VALUES
('What is cosine similarity',
 'Cosine similarity measures the angle between two vectors in a multi-dimensional space. In our chatbot, it compares the TF-IDF vector of your query against FAQ vectors. A score of 1.0 means identical direction, and 0.0 means completely different. We use a threshold of 0.15 to determine if a match is good enough.',
 'chatbot',
 'cosine,similarity,score,measure,compare,vector,angle,distance,matching,comparison');

-- ── Technology Stack ───────────────────────────────────────────────────
INSERT INTO faq_entries (question, answer, category, keywords) VALUES
('What technology stack is used',
 'The backend uses Java 21 LTS with Spring Boot 3.4.3, Spring WebSocket, Spring Data JPA, and H2 Database. The frontend uses HTML5, CSS3, JavaScript, SockJS, and STOMP.js. The chatbot is built with a custom TF-IDF engine in pure Java.',
 'technology',
 'technology,stack,tech,technologies,tools,framework,frameworks,language,languages,built,made,backend,frontend,used,using');

INSERT INTO faq_entries (question, answer, category, keywords) VALUES
('What is WebSocket',
 'WebSocket is a communication protocol that provides full-duplex communication channels over a single TCP connection. Unlike HTTP, it allows the server to push data to clients in real-time without the client having to request it. In this app, WebSocket enables instant message delivery.',
 'technology',
 'websocket,ws,socket,protocol,connection,communication,full,duplex,realtime,real,time,push,bidirectional');

INSERT INTO faq_entries (question, answer, category, keywords) VALUES
('How does WebSocket messaging work',
 'WebSocket messaging in this app works through the STOMP protocol over SockJS. When a user sends a message, it travels via STOMP to the Spring Boot server, is processed by the ChatController, persisted to the database, and then broadcast to all connected clients on the /topic/public channel in real-time.',
 'technology',
 'websocket,messaging,message,work,stomp,sockjs,broadcast,realtime,real,time,flow,send,receive,protocol');

INSERT INTO faq_entries (question, answer, category, keywords) VALUES
('What is STOMP protocol',
 'STOMP (Simple Text Oriented Messaging Protocol) is a simple messaging protocol that works over WebSocket. It provides a frame-based format for sending and receiving messages, with concepts like SUBSCRIBE, SEND, and MESSAGE frames.',
 'technology',
 'stomp,protocol,messaging,text,oriented,simple,subscribe,send,frame');

INSERT INTO faq_entries (question, answer, category, keywords) VALUES
('What is SockJS',
 'SockJS is a JavaScript library that provides a WebSocket-like API with fallback transport options. If the browser does not support WebSocket, SockJS automatically falls back to alternative transports like long-polling.',
 'technology',
 'sockjs,sock,javascript,fallback,transport,browser,library,polyfill,compatibility');

INSERT INTO faq_entries (question, answer, category, keywords) VALUES
('What database is used',
 'The application uses H2 in-memory database for development, which requires zero configuration. For production, it can be switched to MySQL by updating the application.properties configuration.',
 'technology',
 'database,db,h2,mysql,storage,data,persist,memory,sql,relational');

INSERT INTO faq_entries (question, answer, category, keywords) VALUES
('What is Spring Boot',
 'Spring Boot is a Java framework that simplifies the creation of production-ready applications. It provides auto-configuration, embedded servers, and opinionated defaults, allowing developers to focus on business logic instead of boilerplate setup.',
 'technology',
 'spring,boot,framework,java,backend,server,autoconfiguration,starter');

-- ── Architecture ───────────────────────────────────────────────────────
INSERT INTO faq_entries (question, answer, category, keywords) VALUES
('Explain the architecture',
 'The architecture follows a layered MVC pattern: Controller layer handles incoming requests (both WebSocket and REST), Service layer contains business logic (chat, user management, chatbot TF-IDF), Repository layer manages database access via Spring Data JPA, and the Database layer stores users, messages, and FAQ data.',
 'architecture',
 'architecture,design,structure,pattern,layer,mvc,controller,service,repository,model,overview,explain');

INSERT INTO faq_entries (question, answer, category, keywords) VALUES
('How does real-time messaging work',
 'Real-time messaging works through WebSocket with STOMP protocol. When a user sends a message, it goes to the server via STOMP, is processed by ChatController, saved to the database, and broadcast to all connected clients on the /topic/public channel.',
 'architecture',
 'realtime,real,time,messaging,work,message,flow,broadcast,send,receive,live,instant');

INSERT INTO faq_entries (question, answer, category, keywords) VALUES
('How does user presence tracking work',
 'User presence is tracked through WebSocket session events. When a user connects, they are marked ONLINE in the database. When they disconnect (detected via SessionDisconnectEvent), they are marked OFFLINE and a leave notification is broadcast to all connected users.',
 'architecture',
 'presence,tracking,online,offline,status,user,connect,disconnect,session,active');

-- ── Usage ──────────────────────────────────────────────────────────────
INSERT INTO faq_entries (question, answer, category, keywords) VALUES
('How to run this application',
 'To run: 1) Ensure Java 21 and Maven 3.8+ are installed. 2) Navigate to the project directory. 3) Run: mvn spring-boot:run  4) Open http://localhost:5000 in your browser. The app starts on port 5000.',
 'usage',
 'run,start,launch,execute,setup,install,deploy,command,maven,mvn,running');

INSERT INTO faq_entries (question, answer, category, keywords) VALUES
('How to send a message',
 'After logging in with a username, simply type your message in the input field at the bottom and press Enter or click the Send button. Your message will be broadcast to all connected users in real-time.',
 'usage',
 'send,message,type,write,chat,talk,communicate,input');

INSERT INTO faq_entries (question, answer, category, keywords) VALUES
('Can I see who is online',
 'Yes! The sidebar on the right side of the chat window shows all currently online users with a green indicator. The list updates in real-time as users join or leave.',
 'usage',
 'online,users,list,sidebar,who,active,connected,see,view,present');

INSERT INTO faq_entries (question, answer, category, keywords) VALUES
('How does this chat application work',
 'This chat application works by establishing a WebSocket connection between your browser and the Spring Boot server. When you send a message, it is transmitted via the STOMP protocol, processed by the server, saved to the database, and instantly broadcast to all connected users. The chatbot feature uses TF-IDF to match your @bot queries against a FAQ database.',
 'general',
 'chat,application,work,works,how,function,operate,process,explain,mechanism');

INSERT INTO faq_entries (question, answer, category, keywords) VALUES
('Tell me about this project',
 'This is a full-stack real-time chat application built as a demonstration of modern web technologies. It combines Spring Boot for the backend, WebSocket/STOMP for real-time messaging, H2 database for persistence, and a custom TF-IDF chatbot engine. The frontend features a dark glassmorphism design with responsive layout.',
 'general',
 'project,tell,about,describe,info,information,summary,overview,details');

-- ── Usage (additional) ──────────────────────────────────────────────────
INSERT INTO faq_entries (question, answer, category, keywords) VALUES
('What port does the application run on',
 'The application runs on port 5000. Once started, open your browser and navigate to http://localhost:5000 to access the chat interface. The H2 database console is also available at http://localhost:5000/h2-console.',
 'usage',
 'port,5000,localhost,url,address,access,browser,navigate,open,http');

INSERT INTO faq_entries (question, answer, category, keywords) VALUES
('What happens when I disconnect or leave chat',
 'When you click the logout button or close the browser, the WebSocket connection closes. The server detects the SessionDisconnectEvent, marks your status as OFFLINE in the database, and broadcasts a leave notification to all remaining connected users.',
 'usage',
 'disconnect,leave,logout,offline,close,browser,session,event,notification,quit,exit');

INSERT INTO faq_entries (question, answer, category, keywords) VALUES
('Is there user authentication or login',
 'The application uses a simple username-based entry system rather than full authentication. You choose a username on the login screen, which is used to identify you in chat. There is no password, registration, or session token — it is designed for demonstration purposes.',
 'usage',
 'authentication,login,password,auth,security,username,register,account,session,token,sign');

INSERT INTO faq_entries (question, answer, category, keywords) VALUES
('What does the clear history button do',
 'The clear history button (trash icon in the top header) deletes all stored chat messages from the database via the DELETE /api/messages endpoint, and clears the message display area. This action cannot be undone.',
 'usage',
 'clear,history,delete,trash,button,remove,messages,wipe,reset,clean');

-- ── Features (additional) ───────────────────────────────────────────────
INSERT INTO faq_entries (question, answer, category, keywords) VALUES
('How does the FAQ panel work',
 'The FAQ panel is a slide-in panel accessible by clicking the question mark icon (?) in the top-right header. It displays all FAQ questions grouped by category. Clicking any question automatically sends it to the chatbot so you receive an instant answer.',
 'features',
 'faq,panel,question,mark,questions,list,categories,click,send,slide,open,close,header');

INSERT INTO faq_entries (question, answer, category, keywords) VALUES
('How does the random question feature work',
 'The random question button (dice icon at the bottom-left of the message input) fetches a random FAQ question from the server and automatically submits it to the chatbot. Use it to explore what the chatbot knows.',
 'features',
 'random,question,dice,button,explore,discover,auto,submit,fetch,surprise');

INSERT INTO faq_entries (question, answer, category, keywords) VALUES
('Can multiple users chat at the same time',
 'Yes! This application supports multiple simultaneous users. Each user connects via their own WebSocket session. Messages are broadcast to all connected users in real-time, and the sidebar shows all currently online users.',
 'features',
 'multiple,users,simultaneous,concurrent,same,time,together,participants,group,chat');

INSERT INTO faq_entries (question, answer, category, keywords) VALUES
('How many messages are stored in message history',
 'When you connect to the chat, the last 50 messages are loaded from the database and displayed. This is controlled by the ChatMessageRepository which retrieves the 50 most recent messages ordered by timestamp.',
 'features',
 'history,messages,stored,load,50,limit,count,number,how many,connect,database');

INSERT INTO faq_entries (question, answer, category, keywords) VALUES
('What is the glassmorphism UI design',
 'Glassmorphism is a modern UI design trend that simulates frosted glass: elements appear translucent with a blurred background effect. This app uses dark glassmorphism with CSS backdrop-filter blur, semi-transparent backgrounds, and subtle border highlights for a sleek, modern look.',
 'features',
 'glassmorphism,glass,ui,design,blur,frosted,translucent,backdrop,css,style,dark,theme,aesthetic');

-- ── Chatbot (additional) ────────────────────────────────────────────────
INSERT INTO faq_entries (question, answer, category, keywords) VALUES
('Do I need to use the @bot prefix',
 'Yes. To direct a message to the chatbot, start your message with @bot followed by your question, for example: @bot how does this app work? Regular messages without the @bot prefix are broadcast to all users as normal chat messages.',
 'chatbot',
 'bot,prefix,atbot,@bot,trigger,command,start,question,direct,address');

INSERT INTO faq_entries (question, answer, category, keywords) VALUES
('What happens when the chatbot does not find an answer',
 'When no FAQ entry scores above the 0.15 similarity threshold, the chatbot returns a fallback response suggesting you try rephrasing your question or asking about app features, technology, or architecture.',
 'chatbot',
 'fallback,no match,threshold,score,not found,low,similarity,rephrase,fail,answer,response,unknown');

INSERT INTO faq_entries (question, answer, category, keywords) VALUES
('How many FAQ entries does the chatbot know',
 'The chatbot is seeded with over 40 FAQ entries spanning 6 categories: general, features, chatbot, technology, architecture, and usage. You can view all questions in the FAQ panel (click the ? icon) or via the REST API at GET /api/chatbot/questions.',
 'chatbot',
 'faq,entries,questions,how many,count,database,know,knowledge,categories,total,40');

-- ── Technology (additional) ─────────────────────────────────────────────
INSERT INTO faq_entries (question, answer, category, keywords) VALUES
('What Java version does this application use',
 'This application is built with Java 21 LTS (Long-Term Support) and Spring Boot 3.4.3. Java 21 was chosen for its modern language features and long-term support. The project was upgraded from Java 17 to Java 21 on the appmod/java-upgrade branch.',
 'technology',
 'java,version,21,lts,17,jdk,language,upgrade,runtime,jvm,21,long,term,support');

INSERT INTO faq_entries (question, answer, category, keywords) VALUES
('Does this application have unit tests',
 'Yes! The project includes a comprehensive JUnit 5 test suite with Mockito 5 for mocking. Tests cover services (ChatService, UserService, ChatbotService), controllers, models, and DTOs. JaCoCo is configured for code coverage reporting. Run mvn clean verify to execute all 37 tests.',
 'technology',
 'tests,unit tests,junit,mockito,testing,coverage,jacoco,verify,suite,test,37,spec');

INSERT INTO faq_entries (question, answer, category, keywords) VALUES
('What is Spring Data JPA',
 'Spring Data JPA is a Spring framework module that simplifies database access using the Java Persistence API. It provides repository interfaces (UserRepository, ChatMessageRepository, FaqRepository) where you declare methods and Spring automatically generates the SQL queries.',
 'technology',
 'spring,data,jpa,repository,hibernate,orm,sql,query,persistence,database,entity');

INSERT INTO faq_entries (question, answer, category, keywords) VALUES
('What is Hibernate',
 'Hibernate is an Object-Relational Mapping (ORM) framework that maps Java entities (User, ChatMessage, FaqEntry) to database tables automatically. This app uses Hibernate 6 via Spring Data JPA so you interact with plain Java objects rather than writing raw SQL.',
 'technology',
 'hibernate,orm,object,relational,mapping,entity,jpa,table,persist,session,sql,6');

INSERT INTO faq_entries (question, answer, category, keywords) VALUES
('How do I access the H2 database console',
 'The H2 database console is available at http://localhost:5000/h2-console while the app is running. Use JDBC URL: jdbc:h2:mem:chatdb, Username: sa, and leave the password blank. From there you can inspect tables and run SQL queries directly.',
 'technology',
 'h2,console,database,admin,browser,jdbc,sql,inspect,tables,query,mem,chatdb,5000');

INSERT INTO faq_entries (question, answer, category, keywords) VALUES
('How do I switch to MySQL for production',
 'To switch from H2 to MySQL: 1) Uncomment the MySQL configuration lines in application.properties. 2) Set the datasource URL, username, and password. 3) Change ddl-auto to update. 4) Add the MySQL JDBC connector to pom.xml. Hibernate will create tables automatically.',
 'technology',
 'mysql,production,switch,configure,datasource,properties,jdbc,database,deploy,prod,connector');
