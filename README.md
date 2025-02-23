# CineQuizRoyale
Mobile App to increase cinema culture

Increasing Cinema Culture – Many young people enjoy movies but may not have deep knowledge about cinema history, different genres, or classic films. By offering a fun and competitive way to learn through trivia, the app encourages users to expand their knowledge.
Motivating Engagement Through Rewards – Young audiences may need extra incentives to stay engaged. By offering real-world rewards like free movie tickets, discounts, or exclusive screenings, the app provides a tangible reason for users to participate and improve their movie knowledge.
Connecting Movie Enthusiasts – The app serves as a social platform where users can compete with others who have similar cinematic tastes. This helps create a community of film lovers, fostering discussions and recommendations about movies.
Encouraging Cinema Attendance – By rewarding top players with tickets to their nearest cinema, the app also promotes theater visits, which helps support local cinemas and the film industry.

Potential users for the app.

📱 Young Audiences & Students
Teens and young adults who consume a lot of digital entertainment but may not go to the cinema as often. Rewards like free movie tickets could motivate them to visit theaters more frequently.
🎬 Movie Enthusiasts
People who love watching movies and want to test their knowledge while competing with others. They may already follow film awards, directors, and genres closely.
🍿 Casual Moviegoers
Users who enjoy movies but may not be experts. They will be attracted by the rewards (free tickets) and the fun of learning more about cinema.
🌍Social & Community Seekers
Users who want to connect with others who share their movie tastes. The app can help them find friends with similar interests and engage in discussions.


Main features (short description and functionality).
Main Features
✅ Persistent Storage

Stores user progress, scores, rewards, and preferences to ensure data is saved across sessions.
Allows users to track their trivia history and achievements.

✅ Maps or Location Services

Identifies the closest participating cinemas to offer relevant rewards (e.g., free tickets).
Provides directions to theaters where users can redeem their prizes.

✅ Interaction with External Services

Integrates with payment systems (if premium features exist) and reward distribution platforms.
Connects with movie databases (IMDb, TMDb, etc.) for updated trivia content.
Supports social media sharing to increase engagement.

✅ Background Services, Notifications, or Alarms

Sends reminders for daily challenges, leaderboard updates, and new trivia events.
Notifies users when they earn rewards or when a new quiz is available.

Secondary Features
📌 Using Third-Party Libraries

Utilizes libraries for UI design, database management, and API interactions.
May include frameworks for animations, charts, or social media integration.

📌 Deployment on Physical Devices

Ensures the app runs smoothly on real smartphones and tablets.
Optimizes for different screen sizes and operating systems.

📌 Automated Tests

Implements unit tests and UI testing to ensure app stability.
Runs automated tests for different devices and OS versions.

📌 Other Features

Dark Mode: Enhances the user experience for nighttime use.
User Profiles & Customization: Allows players to set avatars, nicknames, and preferred genres.
Multiplayer Mode: Enables users to challenge friends or compete in real-time quizzes.

Initial prototyping (i.e., set of screens) for the app.
1. Splash Screen
📌 Purpose: Displays the app logo and tagline while loading.
🔹 Animation of the logo or movie-related visuals (e.g., clapperboard).

2. Sign-Up / Login Screen
📌 Purpose: Allows users to create an account or log in.
🔹 Sign in via email, Google, or social media.
🔹 Option to continue as a guest (limited features).

3. Home Screen (Dashboard)
📌 Purpose: Main hub for navigation and quick access.
🔹 "Start Quiz" button for instant gameplay.
🔹 Leaderboard preview (top players of the week).
🔹 Display of available rewards and challenges.

4. Quiz Screen
📌 Purpose: Displays trivia questions and handles user interactions.
🔹 Multiple-choice or true/false questions.
🔹 Timer for each question.
🔹 Score tracking and progress bar.
🔹 Fun animations when answering correctly/incorrectly.

5. Leaderboard Screen
📌 Purpose: Shows rankings of top players.
🔹 Weekly, monthly, and all-time rankings.
🔹 User’s current position highlighted.
🔹 Option to filter by location, genre, or friends.

6. Rewards & Prizes Screen
📌 Purpose: Displays prizes users can earn.
🔹 Free cinema tickets, discounts, or exclusive screenings.
🔹 Claim button for eligible rewards.
🔹 Integration with location services to find the nearest cinema.

7. Profile & Settings Screen
📌 Purpose: User profile customization and preferences.
🔹 Avatar selection, username, and favorite movie genres.
🔹 Notification settings for reminders and updates.
🔹 Linked accounts (social media, email, etc.).

8. Cinema Locator Screen
📌 Purpose: Helps users find the nearest cinema.
🔹 Interactive map with pins for nearby theaters.
🔹 Distance and directions.
🔹 Partner cinemas for redeeming rewards.

9. Notifications Screen
📌 Purpose: Keeps users engaged with reminders.
🔹 Alerts for new quizzes, leaderboard updates, and available rewards.
🔹 Social invites (challenges from friends).

Initial roadmap (milestones, timing, distribution of work)
Week 1-2: Planning & Prototyping
Define core features, create wireframes, set up GitHub repository, choose tech stack.
Week 3-4: Frontend & Backend Setup
Implement authentication, create UI components, set up database.
Week 5-6: Quiz System Development
Develop question system, timer, scoring logic, API for movie trivia.
Week 7-8: Rewards & Leaderboard
Implement reward tracking, integrate cinema location API, create leaderboard logic.
Week 9-10: Social Features & Notifications
Add multiplayer challenges, push notifications, social media integration.
Week 11-12: Testing & Bug Fixes
Conduct automated and user testing, optimize UI/UX, fix issues.
Week 13: Final Touches & Deployment Prep
Final UI refinements, security checks, app store listing preparation.

Strategy for collaborative development (e.g., using GitHub or other mechanism).

✅ **Version Control & Collaboration**  
- Use **GitHub** (or GitLab) for source code management.  
- Follow a **branching strategy** (main → dev → feature branches).  
- Conduct **code reviews via pull requests**.  

✅ **Task Management**  
- Use **Trello / Jira** to assign tasks, track progress, and manage sprints.  
- Define **weekly sprint goals** to keep the team aligned.  

✅ **Communication**  
- Set up **Slack / Discord** for team discussions.  
- Use **Google Meet / Zoom** for weekly check-ins.  

✅ **CI/CD & Deployment**  
- Automate builds & testing with **GitHub Actions** or **Jenkins**.  
- Deploy test versions using **Firebase / TestFlight** for beta testing.  

