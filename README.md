# CineQuiz Royale

## 1. Introduction

CineQuiz Royale is a fun and engaging mobile app that challenges movie lovers with trivia questions about cinema. As you answer correctly, you earn points that can be redeemed for tickets at independent cinemas. The goal is to encourage people to go to the movies more often while supporting smaller theaters and expanding their film knowledge. Whether you're a casual viewer or a true cinephile, CineQuiz Royale makes learning about film history and culture both exciting and rewarding. 

## 2. Persistent Storage: Cloud Storage

Before going through the explanation of our app, it is important to understand how we saved all data.

In Cloud Storage, we created a bucket named `cinequizroyale-bucket1`. This bucket has different folders that save all data needed. As you can see in the code, the folder `com/example/cinequizroyale/utils` has several files such as `CinemaDataParser.kt` or `QuestionUploader.kt` that manage data submission to Cloud, and once saved, there is no need to run it again. 

Data is saved in JSON format so we can have variables associated with a unique identifier. For example, user data or cinema data. Here we provide the structure of the bucket:

cinequizroyale-bucket-1/
├── users/
│ ├── {userId}.json
│ └── friends/
│ └── {userId}.json
├── redemptions/
│ └── {userId}/
│ ├── active.json
│ └── history.json
├── cinemas/
│ └── {cinemaId}.json
├── questions/
│ ├── {questionId}.json
│ └── metadata.json
├── friends/
│ └── {userId}.json

- **Basic user info + points**
- **List of friend userIds or friend relationships**
- **Currently active redemptions (prizes to redeem)**
- **Past redemption history**
- **Cinema info (location, name, etc.)**
- **Individual quiz question**
- **Metadata like categories, difficulty levels, etc.**
- (Alternative to users/friends) Global or indexed friendships

## 3. Persistent Design

The `res` (resources) folder in CineQuiz Royale is foundational to maintaining a persistent and immersive design. Apart from predetermined files, we enriched this folder with the following:

### Drawable Resources – Visual Consistency

The drawable directory contains images and graphical assets that uphold the app's visual style across screens:

- **Movie Posters** like `blade_runner.jpg`, and `dune_part_two.jpg` provide consistent visuals for quiz content.
- **UI Elements** such as `backarrow.png`, `photouser.png`, and `popcorn.jpg` create recognizable navigation and interaction points.
- **XML Drawables** like `rounded_button.xml` ensure UI components maintain a branded, custom appearance across devices.
- **App Icons** defined through XML ensure uniform branding on launchers and task switchers.

### Font Resources – Branding Through Typography

Custom fonts like `caveat.ttf` (handwritten style) and `luckiest_guy.ttf` (playful, bold) reinforce the app’s cinema-inspired, entertainment-focused theme. These fonts were imported from Google Fonts.

### Layout Resources – Structural Uniformity

Layout XML files such as `activity_maps.xml` for showing cinema locations and `item_movie.xml` for listing quiz entries define the structure and ensure screens adhere to a uniform layout and interaction pattern, reinforcing familiarity for the user.

### Configuration Resources – Thematic and Visual Coherence

The `values` folder ties the visual and textual elements together:

- **colors.xml**: Centralizes the app's color palette for a consistent visual theme.
- **strings.xml**: Localizes text content.
- **styles.xml** and **themes.xml**: Define overarching visual styles, ensuring all screens and components follow the same theme.

Overall, we have implemented Google Fonts and secondary features like smooth navigation and adaptive layouts to enhance the user experience across different platforms and screen sizes. These tools allow us to provide a visually appealing and functional interface.

## 4. Implementation

### Login Screen

This screen is the entry point for the app, ensuring that only signed-in users can access the rest of the features. It presents a Google Sign-In button to authenticate users. 

To implement it, we use `GoogleSignInAccount`, from the Google Sign-In API. When the user clicks the ‘Sign in with Google’ button, the `onGoogleSignIn` function is triggered, launching the Google authentication process. Once authenticated, the app retrieves the user’s Google account details, such as their name and email and grants access to the main screen.

#### Screenshot from Login Screen

---

### Main Screen

The Main Screen is the central hub of the app, where users can access different features after signing in. It welcomes the user with a personalized greeting that displays their Google account name. The UI maintains the same structure as the Login screen.

#### Overview of the key features in the Main Screen

- **User Profile**: At the top of the screen, there is a square that displays the user’s profile picture, name, email, and current points. The information is retrieved from their Google account.
- **Friends**: Under the user profile, there is a button called ‘friends’ where you can see other friends' current points.
- **Start Playing**: Below the User Profile and Friends button, you can find a large button labeled ‘Start Playing’ that navigates the user to the quiz section.
- **Metacritic Review**: It navigates the user to the Metacritic page, so you are able to check the reviews for any movie you are looking for.
- **Bottom Navigation Buttons**:
  - **Cinemas**: Navigates you to a new screen where you can find cinemas near you.
  - **Prizes**: Takes the user to the prize redemption screen.
  - **History**: Opens the redemption history screen.
- **Logout Button**: Allows users to sign out of their account, returning them to the Login Screen.

#### Screenshot from Main Screen

---

### Profile Screen

When you click on the profile card, it takes you to a screen where you can see your name, surname, email, and other personal information. Most importantly, it displays the points you have accumulated.

#### Screenshot of the Profile Screen

---

### Friends Screen

Once you click on the friends button, you will be able to see what your other friends are doing in the app by seeing how many points they have.

How it works:
- First, we load the friends from a JSON file stored in Google Cloud Storage, with information about the id, name, and points.
- The function filters the current user’s id so they do not appear in their own friends list.
- Finally, when the data is ready, it displays the name and the points of each one of the friends in its own card, providing a simple and clean way to see how friends are doing in the game.

#### Screenshot of the Friends Screen

---

### CinemaQuestions Screen

The `Start Playing` button lets users begin the quiz. When clicked, it takes them from the Main Screen to the CinemaQuestionsScreen, where they can answer movie-related questions. Once the quiz is completed, their points are updated, and they can return to the main menu. 

The questions are fetched from Google Cloud Storage, where they are stored in a JSON file (`questions` folder). When the quiz starts, the app requests this file using the Google Cloud Storage API.

#### Screenshot of the Quiz Screen

---

### Metacritic Screen (Use of Web Services)

The Metacritic screen allows you to search for any movie you want and see the related reviews, the movie’s rating, and whether it is really worth watching or not.

The webpage is shown using a built-in browser inside the app, with JavaScript enabled so the website works properly. 

#### Screenshot of the Metacritic Screen

---

### Cinemas Screen

The app accesses the Google Maps API effectively while obtaining the user's precise location if they approve the permissions.

#### Screenshots of Permission Request and Cinema Screen

It works by showing a Google Map with markers representing cinemas. Once the data is ready, each cinema is added as a marker on the map.

---

### Prize Redemption Screen

The screen allows users to exchange the points they earn from quizzes for rewards. It provides a visual representation of available prizes and their required points.

#### Screenshots of the Redeemable Prizes Screen

---

### Track History Screen

The Redemption History Screen allows users to track the prizes they have redeemed. The app updates this screen by fetching the latest data from Google Cloud Storage.

#### Screenshot of the History Track Screen

---

### Key Functionalities

- **Active Prize Management**: The app manages active and used prizes. Users can mark prizes as used.
- **Cloud-Based Persistence**: All redemptions are stored in Google Cloud Storage under two different files: `redemptions/{account.id}/active.json` and `redemptions/{account.id}/history.json`.
- **Prize Usage Workflow**: The app tracks the user’s points, redemptions, and status.

---

### Special Features

1. **Authorization Handling**: Handles any user authorization issues that may arise.
2. **UI Feedback with Notifications**: Toast messages inform the user about successful operations or errors.

---

## Considerations

When running the application:
- It must be executed on a **Pixel 4 API 30** emulator.
- An active internet connection is required.
- Google Play Services must be installed and up to date.

---

## Summary and Outlook

CineQuiz Royale offers a fun and educational experience, combining trivia with rewards. It features personalized profiles, a friends leaderboard, movie reviews, and nearby cinemas. Users can redeem points for prizes like cinema tickets and discounts, helping support independent theaters.

