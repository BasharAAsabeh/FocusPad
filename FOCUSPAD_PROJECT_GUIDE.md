# FocusPad Project Guide

## Overview

FocusPad is a JavaFX desktop productivity app built as a single `App` class. It includes:

- welcome, login, register, dashboard, tasks, calendar, and settings pages
- dark sidebar navigation
- modern card-based layout
- login/register validation
- task management
- calendar session management
- theme switching
- profile photo upload with circular cropping

The app is currently data-driven in memory, but it is structured so a real database can be added later with minimal changes.

Main files:

- [`App.java`](./src/main/java/App.java)
- [`pom.xml`](./pom.xml)

## Big Picture Architecture

The app uses a scene-based structure:

1. The program starts on the welcome page.
2. The user goes to login or register.
3. Successful login or registration takes the user to the dashboard.
4. The sidebar lets the user move between dashboard, tasks, calendar, and settings.
5. Data is kept in memory for now, but the code has TODO markers where database logic should later go.

This makes the app easy to demo now and easy to connect to MySQL later.

## Main Class

The whole UI lives inside:

- [`App.java`](./src/main/java/App.java)

It extends `Application`, which is the standard JavaFX entry point.

### Why one class?

For a university project, keeping everything in one class makes the flow easier to explain and submit. It still uses OOP ideas through enums, inner classes, helper methods, and reusable builders.

## `Page` Enum

The `Page` enum is a fixed list of all app screens:

- `WELCOME`
- `LOGIN`
- `REGISTER`
- `DASHBOARD`
- `TASKS`
- `CALENDAR`
- `SETTINGS`

### Why it exists

`Page` acts like a screen identifier. Instead of using strings, the code uses strong typed enum values when switching scenes.

Example:

```java
showPage(stage, Page.TASKS);
showPage(stage, Page.SETTINGS);
```

### How to explain it

You can say:

> `Page` is an enum that acts like a navigation controller. It gives the app a safe and readable way to switch between screens.

## `Theme` Enum

The `Theme` enum currently has:

- `LIGHT`
- `DARK`

It controls whether the app uses a light or dark color palette.

## Data Models

The app uses inner classes to represent its main data.

### `User`

Fields:

- `username`
- `email`
- `password`
- `avatarPath`
- `tasks`
- `sessions`

This is the account object. Each user owns their own tasks and calendar sessions.

### `Task`

Fields:

- `id`
- `title`
- `priority`
- `deadline`
- `completed`

This represents one task in the task list.

### `StudySession`

Fields:

- `id`
- `title`
- `details`

This represents a calendar session or event.

## Where Data Is Stored

The app currently stores users in:

- `Map<String, User> usersByEmail`

This is in-memory storage, not a real database.

What that means:

- accounts exist while the app is running
- tasks exist while the app is running
- sessions exist while the app is running
- closing the app clears everything

The code contains `TODO` comments where database logic should later replace the in-memory version.

## Scene Structure

The app uses one `Scene` per page:

- `welcomeScene`
- `loginScene`
- `registerScene`
- `homeScene`
- `tasksScene`
- `calendarScene`
- `settingsScene`

Each scene is built by its own method:

- `createWelcomeScene(Stage stage)`
- `createLoginScene(Stage stage)`
- `createRegisterScene(Stage stage)`
- `createHomeScene(Stage stage)`
- `createTasksScene(Stage stage)`
- `createCalendarScene(Stage stage)`
- `createSettingsScene(Stage stage)`

### Why scenes?

Scenes are the cleanest way to swap entire pages in JavaFX. A scene change is basically a page change.

## Page Breakdown

### Welcome Page

Layout:

- centered `VBox`
- `StackPane` background
- logo
- title and subtitle
- illustration made from JavaFX shapes
- login and register buttons

### Logo and Brand Mark

The FocusPad "FP" logo is not downloaded from an image site or logo pack.

It is built directly in JavaFX code using:

- a `Label` with the text `FP`
- rounded background styling
- a gradient fill
- `DropShadow`
- shapes like `Circle` and `Rectangle` for the surrounding visual treatment

The smaller sidebar brand mark uses the same idea.

### Where the icons came from

The sidebar and button icons come from the Ikonli FontAwesome pack.

That means:

- the icons are not custom-drawn in the app
- they are taken from the FontAwesome icon set through Ikonli
- they are inserted in Java code using the `icon(...)` helper

So the answer is:

> The logo was built manually in JavaFX code, and the interface icons come from FontAwesome via Ikonli.

### Login Page

Layout:

- centered card
- email field
- password field
- error label
- login button
- register link

Validation:

- empty fields are blocked
- email format is checked
- credentials must match an existing user

### Register Page

Layout:

- centered card
- username field
- email field
- password field
- confirm password field
- error label
- register button
- login link

Validation:

- no empty fields
- username length checked
- valid email checked
- duplicate emails blocked
- password confirmation must match
- password minimum length checked

### Dashboard

Layout:

- `BorderPane`
- left sidebar
- main content inside a scrollable center region

Sections:

- stats cards
- today’s focus
- quick actions
- recent tasks

### Tasks Page

Layout:

- sidebar
- input row for new tasks
- filter buttons
- `ListView<Task>` inside a card

This page is important because it uses:

- `ObservableList<Task>`
- `ListView<Task>`
- custom cell rendering

Supports:

- add task
- edit task
- delete task
- complete task
- filters for all/completed/pending

### Calendar Page

Layout:

- sidebar
- header row
- calendar card
- upcoming sessions card

Supports:

- date picker
- dynamically generated month grid
- add session flow

### Settings Page

Layout:

- profile card
- appearance card
- logout button

Supports:

- username change
- optional password change
- profile photo upload
- light/dark theme switching
- logout

## Layouts Used

The project uses standard JavaFX layouts.

### `StackPane`

Used for:

- welcome page background
- avatar composition
- illustration layering

Why:

- It stacks nodes on top of each other.
- It is perfect for centered or circular visuals.

### `VBox`

Used for:

- forms
- cards
- sections of each page

Why:

- It arranges things vertically.
- It is very readable for forms and content blocks.

### `HBox`

Used for:

- button rows
- top headers
- task row content
- stats row

Why:

- It places controls horizontally.
- Useful for toolbars and compact UI sections.

### `BorderPane`

Used for:

- dashboard
- tasks
- calendar
- settings

Why:

- It gives a classic app shell:
  - sidebar on the left
  - content in the center

### `GridPane`

Used for:

- calendar month grid

Why:

- It is the best fit for calendar-style cell placement.

### `ScrollPane`

Used for:

- dashboard and content pages

Why:

- It keeps the layout usable when the content gets taller than the window.

### `Region`

Used for:

- spacers
- pushing content apart

Why:

- It creates flexible empty space inside rows and columns.

## Styling Approach

The app uses inline JavaFX styles and helper methods instead of a separate CSS file.

### Main style helpers

- `primaryButton(String text)`
- `secondaryButton(String text)`
- `dangerButton(String text)`
- `sidebarButton(String text, String iconCode, boolean active)`
- `title(String text)`
- `subtitle(String text)`
- `input(String prompt)`
- `passwordInput(String prompt)`
- `card()`
- `statCard(...)`
- `taskItem(...)`

### Why this approach

For a student project, this keeps the UI logic in one file and makes it easier to explain. A CSS file would also work, but this version is simpler to follow during a presentation.

### Color system

Main constants:

- `PRIMARY`
- `PRIMARY_DARK`
- `DANGER`
- `SUCCESS`
- `WARNING`

Theme-aware helpers:

- `bgColor()`
- `surfaceColor()`
- `surfaceAltColor()`
- `sidebarColor()`
- `textColor()`
- `mutedColor()`
- `borderColor()`

These helpers make the whole app look consistent across scenes.

## Hover Effects

Hover effects are handled through:

- `installButtonStyles(...)`

This helper sets:

- normal style
- hover style
- hand cursor

It is used for:

- primary buttons
- secondary buttons
- danger buttons
- sidebar buttons
- filter buttons
- link buttons

## Icons

The app uses FontAwesome-style icons through Ikonli.

Dependencies in `pom.xml`:

- `ikonli-javafx`
- `ikonli-fontawesome5-pack`

The helper method:

- `icon(String iconCode, int size, String color)`

creates reusable icons for:

- home
- tasks
- calendar
- settings
- logout
- save
- edit
- trash
- theme icons

## Navigation Flow

Navigation is handled by:

- `showPage(Stage stage, Page pageToShow)`

This method:

- rebuilds scenes when needed
- switches the stage scene
- preserves maximized/full-screen state

Other related helpers:

- `applyTheme(...)`
- `logout(...)`
- `resizeStage(...)`

Sidebar navigation:

- Dashboard -> dashboard scene
- Tasks -> tasks scene
- Calendar -> calendar scene
- Settings -> settings scene
- Logout -> login scene

## Window Resize Bug Fix

One bug that was fixed is that scene switching used to interfere with maximized/fullscreen mode.

Now `showPage(...)`:

- stores current size/state
- switches the scene
- restores fullscreen/maximized on the next JavaFX pulse

That prevents the scene switch from unexpectedly resizing the window.

## Calendar Logic

The calendar is now dynamic, not hardcoded to a fixed June sample.

What it does:

- uses a `DatePicker`
- updates the month title when the date changes
- generates the month grid from the selected month
- highlights the selected date
- uses the selected date when adding a session

Main helper:

- `monthGrid(LocalDate monthAnchor, LocalDate selectedDate)`

## Settings and Profile Photo

Settings now includes a profile photo picker.

How it works:

- the user chooses an image file with `FileChooser`
- the file path is saved in `avatarPath`
- the image is displayed in a circular avatar
- the image is center-cropped using `Rectangle2D`

Helper:

- `setCircularAvatarImage(ImageView target, Image image)`

Why this is good:

- portrait photos stay centered
- landscape photos stay centered
- the image fills the circle cleanly

## Tasks Page With `ObservableList` and `ListView`

This is one of the most important JavaFX parts of the app.

The Tasks page uses:

- `ObservableList<Task>`
- `ListView<Task>`
- custom cell rendering

### Why this matters

`ObservableList` automatically notifies the UI when its contents change. `ListView` displays the list. Together, they are the standard JavaFX way to show dynamic collections.

### How it works

- `observableTasks` stores the visible tasks
- `taskListView.setItems(observableTasks)` binds the list to the UI
- `refreshTaskListView(...)` fills it with the current filtered tasks
- each `ListView` cell uses `taskItem(task, stage)` to render the row

### What you can say

> I used `ObservableList` with `ListView` so task updates automatically show in the UI. That is the JavaFX way to handle dynamic lists cleanly.

## Validation Summary

### Login

- empty fields blocked
- email format checked
- password checked against saved user

### Register

- username required
- email required
- unique email enforced
- passwords must match
- password length checked

### Tasks

- task title required
- task title length checked
- priority required
- deadline required

### Calendar Sessions

- session title required

### Settings

- username required
- username length checked
- password length checked if changed

## Database Readiness

The app is not connected to MySQL yet, but it is ready for it.

Current database TODO points:

- login validation
- register user
- load tasks
- insert task
- update task
- delete task
- update profile data
- save avatar path
- save sessions

## Good Short Explanation

If someone asks what the app is, you can say:

> FocusPad is a JavaFX productivity app with a welcome flow, account validation, a dashboard, task management using `ObservableList` and `ListView`, a dynamic calendar, and settings for profile customization and theme switching. The app is currently in-memory, but the code is organized so a database can be added later.

## Current Limitations

Be honest if asked:

- data is still in memory
- the database has not been connected yet
- the app resets when it closes

## Useful Files

- [`App.java`](./src/main/java/App.java)
- [`pom.xml`](./pom.xml)
