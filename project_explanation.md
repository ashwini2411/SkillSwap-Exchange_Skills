# SkillSwap Platform - Project Documentation

This guide provides a comprehensive breakdown of the **SkillSwap Platform**, designed to help you understand and present the architecture, components, required tools, and data flow of the project to your teacher.

---

## 1. Project Overview & Purpose

**SkillSwap** is a web-based educational platform that allows users to trade skills with one another. Instead of paying money for tutoring, users "swap" their knowledge. For example, if User A knows Java and wants to learn French, they can connect with User B who knows French and wants to learn Java.

It provides a secure environment for:
*   User registration and authentication.
*   Profile creation (listing skills offered and skills wanted).
*   Searching for users with specific skills.
*   Sending, accepting, and rejecting swap requests.
*   Real-time chat functionality for matched users.
*   Real-time chat functionality for matched users.
*   **Virtual Economy ("Swap Coins")** for time-banking and rewarding teachers.
*   **AI-Generated Curriculum Roadmaps** to give students a structured 4-week syllabus.
*   **Gamification (Verified Badges)** to give users "Proof-of-Skill" certifications on their profiles.
*   An Administrative dashboard with **Automated 24-hour cleanup** for managing the platform.

---

## 2. Required Technologies (The Tech Stack)

The project uses a modern Java-based web architecture. Here is everything required and what it is used for:

### Backend (Server & Logic)
*   **Java 17**: The core programming language.
*   **Spring Boot (3.2.3)**: The foundational framework. It removes the need for complex configuration and provides a ready-to-run environment with a built-in Tomcat web server.
*   **Spring Security**: Used for authenticating (logging in) users, securely hashing passwords (using BCrypt), and managing roles (User vs. Admin).
*   **Spring Data JPA / Hibernate**: Used to interact with the database without writing raw SQL queries. It manages the connection between Java classes (Entities) and database tables.
*   **Lombok**: A helpful library that automatically generates repetitive code (like Getters and Setters) to keep Java files clean.

### Frontend (User Interface)
*   **Thymeleaf**: A server-side Java template engine. It takes pure HTML files and dynamically injects Java data into them before sending them to the user's browser (e.g., displaying `Welcome, John!` instead of `Welcome, [[name]]!`).
*   **HTML/CSS/JavaScript**: Standard web technologies for structure, styling, and basic interactivity.

### Database
*   **MySQL**: The relational database management system where all persistent data is stored (Users, Messages, Swap Requests, etc.).

---

## 3. Project Architecture (MVC Pattern)

The project follows the standard **Model-View-Controller (MVC)** architectural pattern. This separates the application into three main logical components:

1.  **Model (Entities & Repositories)**: Represents the data. It defines what a `User` or a `SwapRequest` is and handles fetching/saving this data to the MySQL database.
2.  **View (Thymeleaf Templates)**: What the user sees on their screen (the HTML pages).
3.  **Controller (Java Controllers)**: The "brain" in the middle. It receives the user's request (e.g., "I want to log in"), talks to the Model to get/check the data, and then returns the correct View with the data.

---

## 4. Detailed Component Breakdown

Here is a detailed look at every major piece of the project code:

### A. Entities (`com.skillswap.platform.entity`)
### A. Entities (`com.skillswap.platform.entity`)
Entities are standard Java classes marked with `@Entity`. Each class represents a table in the database, and each variable represents a column.
*   `User.java`: Stores user details (username, email, encrypted password, role, skills offered, skills wanted, **Swap Coins balance**, and **Gamification Badges**).
*   `SwapRequest.java`: Represents a request sent from one user to another. Stores the sender, receiver, request status (PENDING, ACCEPTED, REJECTED, **COMPLETED**), and a **dynamically generated AI Roadmap text**.
*   `Message.java`: Represents a chat message sent between two users in a chat room.
*   `Notification.java`: Stores alerts for the user (e.g., "You have a new swap request"). It works alongside an automated 24-hour cleanup scheduler.
*   `Review.java`: Allows users to leave a rating and feedback for an exchanged skill.

### B. Repositories (`com.skillswap.platform.repository`)
Repositories are interfaces extending `JpaRepository`. They provide built-in methods to perform database operations (CRUD - Create, Read, Update, Delete) without writing SQL.
*   `UserRepository.java`: Used to find users by email, or search for users by the skills they offer.
*   `SwapRequestRepository.java`: Used to find all pending requests for a specific user to display a notification counter.
*   *(Includes `MessageRepository`, `NotificationRepository`, `ReviewRepository` for managing their respective data)*

### C. Controllers (`com.skillswap.platform.controller`)
Controllers handle the HTTP requests (GET, POST) from the user's browser.
*   `AuthController.java`: Manages the registration (Signup) and login pages. Processes the form submissions and saves the user to the DB.
*   `DashboardController.java`: Loads the main dashboard after login, fetching the user's specific data to display.
*   `SkillSearchController.java`: Takes a search keyword (e.g., "Python"), queries the `UserRepository` for matching users, and shows the results page.
*   `SkillSearchController.java`: Takes a search keyword (e.g., "Python"), queries the `UserRepository` for matching users, and shows the results page.
*   `SwapRequestController.java`: Intercepts "Send Request", "Accept", or "Reject" actions. **It manages the Virtual Economy** by verifying and deducting 10 Swap Coins when sending a request, and rewarding 10 Coins to the receiver upon acceptance. It also handles the "Complete Swap" logic to permanently award "Verified Skill" Badges.
*   `ChatController.java`: Loads chat rooms, handles missing messages, and connects to the **`AiRoadmapService`** to instantly generate customized 4-week syllabi for the specific skills being swapped.
*   `AdminController.java`: A restricted area only for admins. It can fetch all users, export data to a CSV file (using the OpenCSV library), and ban users.

### D. Views (`src/main/resources/templates`)
The HTML files that the user interacts with.
*   `index/login/signup.html`: Publicly accessible pages for onboarding.
*   `dashboard.html`: The central hub showing the user's status, notifications, and quick links.
*   `search.html`: The interface for finding skills.
*   `requests.html`: Shows a list of incoming and outgoing swap requests with Accept/Reject buttons.
*   `chat-room.html`: The messaging interface.
*   `admin-dashboard.html`: The control panel for the platform administrator.

---

## 5. Processing Flow: How A Swap Request Works

To explain the logic flow to your teacher, you can use the **Swap Request Process** as an example of how everything connects:

1.  **User Interface (View)**: User A searches for "Math" and finds User B's profile. User A clicks a "Send Swap Request" button on `public-profile.html`.
2.  **HTTP Request**: The browser sends a `POST /requests/send` HTTP request to the backend.
3.  **Controller Routing**: The `SwapRequestController.java` intercepts this specific `/requests/send` URL.
4.  **Business Logic (Controller)**:
    *   The Controller identifies who is sending the request (User A) using Spring Security's context.
    *   It identifies the target receiver (User B) from the data sent in the request.
    *   It creates a new `SwapRequest` Entity object, setting the status to `PENDING`.
5.  **Database Save (Repository)**: The Controller passes the new `SwapRequest` object to the `SwapRequestRepository`, which translates it into an `INSERT INTO swap_requests...` SQL statement and saves it to the MySQL Database.
6.  **Notification (Optional)**: The Controller might also create a `Notification` Entity for User B.
7.  **Response (View)**: The Controller redirects User A back to their dashboard or search page with a "Request Sent Successfully" message.
8.  **Receiver's View**: When User B logs in, the `DashboardController` asks the `SwapRequestRepository` "how many pending requests does User B have?". It passes this number to `dashboard.html` using Thymeleaf, updating the notification counter. User B can then view the request on `requests.html` and click "Accept", triggering a similar update flow across the Swap Coin logic.
9.  **AI Roadmap & Completion (New Flow)**: Inside the newly created chat room, both users can click "Generate Custom AI Syllabus" which dynamically writes a 4-week guided exchange text. Once they finish learning, they click "Complete Swap" to earn a permanent "Verified Skill" Badge on their profile!

---

## 6. How to Run and Present It

When showing this to your teacher, explain the exact boot-up sequence:
1.  **Start the Database**: MySQL must be running to accept connections.
2.  **Configure Credentials**: The `src/main/resources/application.properties` file must contain the correct MySQL username, password, and database name. Spring Boot reads this on startup to connect.
3.  **Run the App**: By executing `start_server.bat` (or running it via the IDE), Maven downloads the dependencies from `pom.xml`, compiles the Java code, checks the database tables via Hibernate (creating them if they don't exist), and starts the built-in Tomcat web server.
4.  **Access**: The user opens a web browser and goes to `http://localhost:8080` to interact with the application.

