import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class App extends Application {
    private static final String PRIMARY = "#6D5DF6";
    private static final String PRIMARY_DARK = "#5947E8";
    private static final String DANGER = "#EF4444";
    private static final String SUCCESS = "#16A34A";
    private static final String WARNING = "#D97706";

    private enum Theme {
        LIGHT, DARK
    }

    private enum Page {
        WELCOME, LOGIN, REGISTER, DASHBOARD, TASKS, CALENDAR, SETTINGS
    }

    private enum TaskFilter {
        ALL, COMPLETED, PENDING
    }

    private static class User {
        private String username;
        private final String email;
        private String password;
        private String avatarPath;
        private final List<Task> tasks = new ArrayList<>();
        private final List<StudySession> sessions = new ArrayList<>();

        private User(String username, String email, String password) {
            this.username = username;
            this.email = email;
            this.password = password;
        }
    }

    private static class Task {
        private final int id;
        private String title;
        private String priority;
        private LocalDate deadline;
        private boolean completed;

        private Task(int id, String title, String priority, LocalDate deadline, boolean completed) {
            this.id = id;
            this.title = title;
            this.priority = priority;
            this.deadline = deadline;
            this.completed = completed;
        }
    }

    private static class StudySession {
        private final int id;
        private String title;
        private String details;

        private StudySession(int id, String title, String details) {
            this.id = id;
            this.title = title;
            this.details = details;
        }
    }

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M/d/yyyy");
    private final DateTimeFormatter displayDateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private final Map<String, User> usersByEmail = new HashMap<>();

    private Scene welcomeScene;
    private Scene loginScene;
    private Scene registerScene;
    private Scene homeScene;
    private Scene tasksScene;
    private Scene calendarScene;
    private Scene settingsScene;

    private ListView<Task> taskListView;
    private ObservableList<Task> observableTasks = FXCollections.observableArrayList();
    private VBox upcomingList;
    private Theme currentTheme = Theme.LIGHT;
    private TaskFilter currentTaskFilter = TaskFilter.ALL;
    private User currentUser;
    private int nextTaskId = 1;
    private int nextSessionId = 1;

    @Override
    public void start(Stage stage) {
        seedDemoData();
        buildScenes(stage);

        stage.setTitle("FocusPad");
        stage.setWidth(1000);
        stage.setHeight(650);
        stage.setMinWidth(850);
        stage.setMinHeight(550);
        stage.setScene(welcomeScene);
        stage.show();
    }

    private void seedDemoData() {
        if (!usersByEmail.isEmpty()) {
            return;
        }

        User demoUser = new User("Bashar", "bashar@example.com", "1234");
        demoUser.tasks.add(new Task(nextTaskId++, "Finish JavaFX UI", "High", LocalDate.of(2026, 6, 6), false));
        demoUser.tasks.add(new Task(nextTaskId++, "Connect MySQL database", "High", LocalDate.of(2026, 6, 5), false));
        demoUser.tasks.add(new Task(nextTaskId++, "Prepare project presentation", "Medium", LocalDate.of(2026, 6, 6), false));
        demoUser.tasks.add(new Task(nextTaskId++, "Setup project structure", "Low", LocalDate.of(2026, 6, 2), true));
        demoUser.sessions.add(new StudySession(nextSessionId++, "JavaFX Project Deadline", "All day"));
        demoUser.sessions.add(new StudySession(nextSessionId++, "Database Testing", "06/05/2026"));
        demoUser.sessions.add(new StudySession(nextSessionId++, "UI Polish", "06/04/2026"));
        demoUser.sessions.add(new StudySession(nextSessionId++, "Study Session", "2:00 PM - 4:00 PM"));
        demoUser.sessions.add(new StudySession(nextSessionId++, "Team Meeting", "7:00 PM - 8:00 PM"));
        usersByEmail.put(normalizeEmail(demoUser.email), demoUser);
    }

    private void buildScenes(Stage stage) {
        welcomeScene = createWelcomeScene(stage);
        loginScene = createLoginScene(stage);
        registerScene = createRegisterScene(stage);
        homeScene = createHomeScene(stage);
        tasksScene = createTasksScene(stage);
        calendarScene = createCalendarScene(stage);
        settingsScene = createSettingsScene(stage);
    }

    // Welcome Page
    private Scene createWelcomeScene(Stage stage) {
        StackPane root = new StackPane();
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: " + appGradient() + ";");

        VBox content = new VBox(18);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(640);

        Label logo = logoMark(62, 20);
        Label appTitle = title("FocusPad");
        appTitle.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 48));

        Label appSubtitle = subtitle("Your day, organized simply.");
        appSubtitle.setFont(Font.font("Verdana", 17));

        StackPane illustration = welcomeIllustration();

        HBox actions = new HBox(14);
        actions.setAlignment(Pos.CENTER);
        Button login = primaryButton("Login");
        Button register = secondaryButton("Register");
        login.setPrefWidth(155);
        register.setPrefWidth(155);
        login.setOnAction(event -> showPage(stage, Page.LOGIN));
        register.setOnAction(event -> showPage(stage, Page.REGISTER));
        actions.getChildren().addAll(login, register);

        content.getChildren().addAll(logo, appTitle, appSubtitle, illustration, actions);
        root.getChildren().add(content);
        return new Scene(root);
    }

    // Login Page
    private Scene createLoginScene(Stage stage) {
        StackPane root = centeredPage();
        VBox form = card();
        form.setMaxWidth(440);
        form.setSpacing(16);
        form.setAlignment(Pos.CENTER_LEFT);

        Label pageTitle = title("Welcome back!");
        Label pageSubtitle = subtitle("Demo login: bashar@example.com / 1234");
        TextField email = input("Email");
        PasswordField password = passwordInput("Password");
        Label error = errorLabel();

        Button login = primaryButton("Login");
        login.setGraphic(icon("fas-sign-in-alt", 14, "#FFFFFF"));
        login.setMaxWidth(Double.MAX_VALUE);
        login.setOnAction(event -> {
            String emailText = email.getText().trim();
            String passwordText = password.getText();

            if (emailText.isEmpty() || passwordText.trim().isEmpty()) {
                error.setText("Please enter your email and password.");
                return;
            }

            if (!isValidEmail(emailText)) {
                error.setText("Please enter a valid email address.");
                return;
            }

            // TODO: Replace this lookup with database login validation.
            User user = usersByEmail.get(normalizeEmail(emailText));
            if (user == null || !user.password.equals(passwordText)) {
                error.setText("Invalid email or password.");
                return;
            }

            currentUser = user;
            error.setText("");
            showPage(stage, Page.DASHBOARD);
        });

        Button register = linkButton("Don't have an account? Register");
        register.setOnAction(event -> showPage(stage, Page.REGISTER));

        form.getChildren().addAll(pageTitle, pageSubtitle, email, password, error, login, register);
        root.getChildren().add(form);
        return new Scene(root);
    }

    // Register Page
    private Scene createRegisterScene(Stage stage) {
        StackPane root = centeredPage();
        VBox form = card();
        form.setMaxWidth(460);
        form.setSpacing(15);
        form.setAlignment(Pos.CENTER_LEFT);

        Label pageTitle = title("Create your account");
        Label pageSubtitle = subtitle("Start organizing your study life.");
        TextField username = input("Username");
        TextField email = input("Email");
        PasswordField password = passwordInput("Password");
        PasswordField confirmPassword = passwordInput("Confirm password");
        Label error = errorLabel();

        Button register = primaryButton("Register");
        register.setGraphic(icon("fas-user-plus", 14, "#FFFFFF"));
        register.setMaxWidth(Double.MAX_VALUE);
        register.setOnAction(event -> {
            String usernameText = username.getText().trim();
            String emailText = email.getText().trim();
            String passwordText = password.getText();
            String confirmText = confirmPassword.getText();

            if (usernameText.isEmpty() || emailText.isEmpty() || passwordText.trim().isEmpty() || confirmText.trim().isEmpty()) {
                error.setText("Please complete all fields.");
                return;
            }

            if (usernameText.length() < 2) {
                error.setText("Username must be at least 2 characters.");
                return;
            }

            if (!isValidEmail(emailText)) {
                error.setText("Please enter a valid email address.");
                return;
            }

            if (usersByEmail.containsKey(normalizeEmail(emailText))) {
                error.setText("An account with this email already exists.");
                return;
            }

            if (passwordText.length() < 4) {
                error.setText("Password must be at least 4 characters.");
                return;
            }

            if (!passwordText.equals(confirmText)) {
                error.setText("Passwords do not match.");
                return;
            }

            User newUser = new User(usernameText, emailText, passwordText);
            usersByEmail.put(normalizeEmail(emailText), newUser);
            currentUser = newUser;

            // TODO: Insert the registered user into the database.
            error.setText("");
            showPage(stage, Page.DASHBOARD);
        });

        Button login = linkButton("Already have an account? Login");
        login.setOnAction(event -> showPage(stage, Page.LOGIN));

        form.getChildren().addAll(pageTitle, pageSubtitle, username, email, password, confirmPassword, error, register, login);
        root.getChildren().add(form);
        return new Scene(root);
    }

    // Home Dashboard
    private Scene createHomeScene(Stage stage) {
        BorderPane root = appShell(stage, Page.DASHBOARD);
        VBox main = mainContent();
        User user = activeUser();

        int totalTasks = user.tasks.size();
        int completedTasks = completedCount(user);
        int pendingTasks = totalTasks - completedTasks;
        double progressValue = totalTasks == 0 ? 0 : (double) completedTasks / totalTasks;
        int progressPercent = (int) Math.round(progressValue * 100);
        Task focusTask = firstPendingTask(user);

        HBox hero = new HBox(18);
        hero.setAlignment(Pos.CENTER_LEFT);
        VBox heroText = new VBox(8);
        Label heading = title("Good evening, " + firstName(user.username));
        Label subheading = subtitle("Here is your productivity overview for today.");
        heroText.getChildren().addAll(heading, subheading);
        Region heroSpacer = new Region();
        HBox.setHgrow(heroSpacer, Priority.ALWAYS);
        HBox dateChip = chip("fas-calendar-day", "June 2026");
        hero.getChildren().addAll(heroText, heroSpacer, dateChip);

        HBox stats = new HBox(18);
        stats.getChildren().addAll(
                statCard("Total Tasks", String.valueOf(totalTasks), "fas-layer-group", PRIMARY),
                statCard("Completed", String.valueOf(completedTasks), "fas-check-circle", SUCCESS),
                statCard("Pending", String.valueOf(pendingTasks), "fas-clock", WARNING)
        );
        for (Node node : stats.getChildren()) {
            HBox.setHgrow(node, Priority.ALWAYS);
        }

        HBox dashboardGrid = new HBox(18);
        VBox focusCard = card();
        focusCard.setMinHeight(210);
        focusCard.setPrefWidth(520);
        Label focusTitle = sectionTitle("Today's Focus");
        Label focusText = bodyText(focusTask == null ? "All tasks are complete. Add a new focus task when you are ready." : focusTask.title);
        Label focusHint = subtitle(totalTasks == 0 ? "Create your first task to start tracking progress." : "Keep momentum with one clear priority.");
        ProgressBar progress = new ProgressBar(progressValue);
        progress.setMaxWidth(Double.MAX_VALUE);
        progress.setPrefHeight(12);
        progress.setStyle("-fx-accent: " + PRIMARY + ";");
        Label percent = new Label(progressPercent + "%");
        percent.setTextFill(Color.web(primaryText()));
        percent.setFont(Font.font("Verdana", FontWeight.BOLD, 13));
        HBox progressRow = new HBox(12, progress, percent);
        progressRow.setAlignment(Pos.CENTER);
        HBox.setHgrow(progress, Priority.ALWAYS);
        focusCard.getChildren().addAll(focusTitle, focusText, focusHint, progressRow);

        VBox quickCard = card();
        quickCard.setMinHeight(210);
        quickCard.setPrefWidth(300);
        HBox addTaskAction = actionRow("fas-plus", "Add a task", "Capture a new study item");
        HBox planAction = actionRow("fas-calendar-plus", "Plan session", "Block focus time");
        HBox reviewAction = actionRow("fas-chart-line", "Review progress", progressPercent + "% completed");
        addTaskAction.setOnMouseClicked(event -> showPage(stage, Page.TASKS));
        planAction.setOnMouseClicked(event -> showPage(stage, Page.CALENDAR));
        quickCard.getChildren().addAll(sectionTitle("Quick Actions"), addTaskAction, planAction, reviewAction);

        HBox.setHgrow(focusCard, Priority.ALWAYS);
        HBox.setHgrow(quickCard, Priority.ALWAYS);
        dashboardGrid.getChildren().addAll(focusCard, quickCard);

        VBox recentCard = card();
        HBox recentHeader = new HBox();
        recentHeader.setAlignment(Pos.CENTER);
        Label recentTitle = sectionTitle("Recent Tasks");
        Button viewAll = secondaryButton("View All Tasks");
        viewAll.setGraphic(icon("fas-arrow-right", 12, primaryText()));
        viewAll.setOnAction(event -> showPage(stage, Page.TASKS));
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        recentHeader.getChildren().addAll(recentTitle, headerSpacer, viewAll);
        recentCard.getChildren().add(recentHeader);
        List<Task> recentTasks = recentTasks(user, 3);
        if (recentTasks.isEmpty()) {
            recentCard.getChildren().add(emptyState("No tasks yet. Add your first task from the Tasks page."));
        } else {
            for (Task task : recentTasks) {
                recentCard.getChildren().add(miniTask(task.title, task.completed));
            }
        }

        main.getChildren().addAll(hero, stats, dashboardGrid, recentCard);
        root.setCenter(wrapScroll(main));
        return new Scene(root);
    }

    // Tasks Page
    private Scene createTasksScene(Stage stage) {
        BorderPane root = appShell(stage, Page.TASKS);
        VBox main = mainContent();
        User user = activeUser();

        Label heading = title("Tasks");
        Label subheading = subtitle("Create, update, complete, and delete your tasks.");
        Label error = errorLabel();

        HBox inputRow = new HBox(12);
        inputRow.setAlignment(Pos.CENTER_LEFT);
        inputRow.setPadding(new Insets(18));
        inputRow.setStyle(panelStyle());
        TextField taskInput = input("What do you want to do?");
        ComboBox<String> priority = new ComboBox<>();
        priority.getItems().addAll("Low", "Medium", "High");
        priority.setValue("Medium");
        priority.setPrefWidth(135);
        priority.setStyle(inputStyle());
        DatePicker deadline = new DatePicker(LocalDate.of(2026, 6, 6));
        deadline.setPrefWidth(155);
        deadline.setStyle(inputStyle());
        Button addTask = primaryButton("Add Task");
        addTask.setGraphic(icon("fas-plus", 13, "#FFFFFF"));
        HBox.setHgrow(taskInput, Priority.ALWAYS);
        inputRow.getChildren().addAll(taskInput, priority, deadline, addTask);

        HBox filters = new HBox(10);
        filters.getChildren().addAll(
                filterButton("All (" + user.tasks.size() + ")", currentTaskFilter == TaskFilter.ALL),
                filterButton("Completed (" + completedCount(user) + ")", currentTaskFilter == TaskFilter.COMPLETED),
                filterButton("Pending (" + pendingCount(user) + ")", currentTaskFilter == TaskFilter.PENDING)
        );
        ((Button) filters.getChildren().get(0)).setOnAction(event -> {
            currentTaskFilter = TaskFilter.ALL;
            showPage(stage, Page.TASKS);
        });
        ((Button) filters.getChildren().get(1)).setOnAction(event -> {
            currentTaskFilter = TaskFilter.COMPLETED;
            showPage(stage, Page.TASKS);
        });
        ((Button) filters.getChildren().get(2)).setOnAction(event -> {
            currentTaskFilter = TaskFilter.PENDING;
            showPage(stage, Page.TASKS);
        });

        taskListView = new ListView<>();
        taskListView.setFocusTraversable(false);
        taskListView.setStyle(listViewStyle());
        taskListView.setItems(observableTasks);
        taskListView.setCellFactory(view -> new ListCell<Task>() {
            @Override
            protected void updateItem(Task task, boolean empty) {
                super.updateItem(task, empty);
                if (empty || task == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                    return;
                }
                setText(null);
                setGraphic(taskItem(task, stage));
                setStyle("-fx-background-color: transparent;");
            }
        });
        refreshTaskListView(stage, user);
        taskListView.setPrefHeight(370);

        addTask.setOnAction(event -> {
            String text = taskInput.getText().trim();
            if (text.isEmpty()) {
                error.setText("Task title cannot be empty.");
                return;
            }

            if (text.length() < 3) {
                error.setText("Task title must be at least 3 characters.");
                return;
            }

            if (priority.getValue() == null) {
                error.setText("Please choose a priority.");
                return;
            }

            if (deadline.getValue() == null) {
                error.setText("Please choose a deadline.");
                return;
            }

            Task task = new Task(nextTaskId++, text, priority.getValue(), deadline.getValue(), false);
            user.tasks.add(task);

            // TODO: Insert the new task into the database for currentUser.email.
            currentTaskFilter = TaskFilter.ALL;
            showPage(stage, Page.TASKS);
        });

        VBox listCard = card();
        listCard.getChildren().addAll(sectionTitle("Task List"), taskListView);

        main.getChildren().addAll(heading, subheading, inputRow, error, filters, listCard);
        root.setCenter(wrapScroll(main));
        return new Scene(root);
    }

    // Calendar Page
    private Scene createCalendarScene(Stage stage) {
        BorderPane root = appShell(stage, Page.CALENDAR);
        VBox main = mainContent();
        User user = activeUser();

        HBox header = new HBox();
        VBox headerText = new VBox(6);
        headerText.getChildren().addAll(title("Calendar"), subtitle("View deadlines and plan study sessions."));
        Button addSession = primaryButton("Add Session");
        addSession.setGraphic(icon("fas-calendar-plus", 13, "#FFFFFF"));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.setAlignment(Pos.CENTER);
        header.getChildren().addAll(headerText, spacer, addSession);

        HBox content = new HBox(18);
        content.setAlignment(Pos.TOP_LEFT);

        VBox calendarCard = card();
        calendarCard.setMinWidth(430);
        Label calendarTitle = sectionTitle(formatMonthYear(LocalDate.of(2026, 6, 1)));
        DatePicker datePicker = new DatePicker(LocalDate.of(2026, 6, 1));
        datePicker.setMaxWidth(Double.MAX_VALUE);
        datePicker.setStyle(inputStyle());
        VBox monthGridHolder = new VBox(10);
        monthGridHolder.getChildren().add(monthGrid(datePicker.getValue(), datePicker.getValue()));
        calendarCard.getChildren().addAll(calendarTitle, datePicker, monthGridHolder);

        VBox upcomingCard = card();
        upcomingCard.setMinWidth(320);
        Label upcomingTitle = sectionTitle("Upcoming");
        TextField sessionInput = input("New study session");
        Label sessionError = errorLabel();
        sessionInput.setVisible(false);
        sessionInput.setManaged(false);
        sessionError.setVisible(false);
        sessionError.setManaged(false);
        upcomingList = new VBox(10);
        for (StudySession session : user.sessions) {
            upcomingList.getChildren().add(upcomingItem(session.title, session.details));
        }
        if (user.sessions.isEmpty()) {
            upcomingList.getChildren().add(emptyState("No sessions yet. Add one from the button above."));
        }
        upcomingCard.getChildren().addAll(upcomingTitle, sessionInput, sessionError, upcomingList);

        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            LocalDate selected = newValue == null ? LocalDate.now() : newValue;
            calendarTitle.setText(formatMonthYear(selected));
            monthGridHolder.getChildren().setAll(monthGrid(selected, selected));
        });

        addSession.setOnAction(event -> {
            if (!sessionInput.isVisible()) {
                sessionInput.setVisible(true);
                sessionInput.setManaged(true);
                sessionError.setVisible(true);
                sessionError.setManaged(true);
                sessionInput.requestFocus();
                return;
            }

            String text = sessionInput.getText().trim();
            if (text.isEmpty()) {
                sessionError.setText("Session title cannot be empty.");
                return;
            }

            StudySession session = new StudySession(
                    nextSessionId++,
                    text,
                    datePicker.getValue() == null ? LocalDate.now().format(displayDateFormatter) : datePicker.getValue().format(displayDateFormatter)
            );
            user.sessions.add(session);

            // TODO: Insert this calendar session into the database for currentUser.email.
            showPage(stage, Page.CALENDAR);
        });

        HBox.setHgrow(calendarCard, Priority.ALWAYS);
        HBox.setHgrow(upcomingCard, Priority.ALWAYS);
        content.getChildren().addAll(calendarCard, upcomingCard);

        main.getChildren().addAll(header, content);
        root.setCenter(wrapScroll(main));
        return new Scene(root);
    }

    // Settings Page
    private Scene createSettingsScene(Stage stage) {
        BorderPane root = appShell(stage, Page.SETTINGS);
        VBox main = mainContent();
        User user = activeUser();

        Label heading = title("Settings");
        Label subheading = subtitle("Customize your FocusPad experience.");

        VBox profileCard = card();
        HBox profileHeader = new HBox(14);
        profileHeader.setAlignment(Pos.CENTER_LEFT);
        StackPane avatar = avatar(firstName(user.username).substring(0, 1).toUpperCase());
        Button photoButton = secondaryButton("Choose Photo");
        photoButton.setGraphic(icon("fas-image", 13, primaryText()));
        Label profileName = new Label(user.username);
        profileName.setTextFill(Color.web(textColor()));
        profileName.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
        VBox profileText = new VBox(6, profileName, subtitle(user.email), photoButton);
        profileHeader.getChildren().addAll(avatar, profileText);
        TextField username = input("Username");
        username.setText(user.username);
        PasswordField newPassword = passwordInput("New Password (optional)");
        Label saveMessage = errorLabel();
        ImageView profilePreview = new ImageView();
        profilePreview.setFitWidth(56);
        profilePreview.setFitHeight(56);
        profilePreview.setPreserveRatio(false);
        profilePreview.setSmooth(true);
        profilePreview.setClip(new Circle(28, 28, 28));

        if (user.avatarPath != null && !user.avatarPath.isBlank()) {
            setCircularAvatarImage(profilePreview, new Image("file:" + user.avatarPath));
        }

        photoButton.setOnAction(event -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Choose a Profile Picture");
            chooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
            );
            java.io.File file = chooser.showOpenDialog(stage);
            if (file != null) {
                user.avatarPath = file.getAbsolutePath();
                setCircularAvatarImage(profilePreview, new Image(file.toURI().toString()));
                avatar.getChildren().setAll(profilePreview);
                // TODO: Save avatarPath to the database for this user.
            }
        });

        if (profilePreview.getImage() != null) {
            avatar.getChildren().setAll(profilePreview);
        }

        Button save = primaryButton("Save Changes");
        save.setGraphic(icon("fas-save", 13, "#FFFFFF"));
        save.setMaxWidth(185);
        save.setOnAction(event -> {
            String newUsername = username.getText().trim();
            String newPasswordText = newPassword.getText();

            if (newUsername.isEmpty()) {
                saveMessage.setTextFill(Color.web(DANGER));
                saveMessage.setText("Username cannot be empty.");
                return;
            }

            if (newUsername.length() < 2) {
                saveMessage.setTextFill(Color.web(DANGER));
                saveMessage.setText("Username must be at least 2 characters.");
                return;
            }

            if (!newPasswordText.isEmpty() && newPasswordText.length() < 4) {
                saveMessage.setTextFill(Color.web(DANGER));
                saveMessage.setText("New password must be at least 4 characters.");
                return;
            }

            user.username = newUsername;
            if (!newPasswordText.isEmpty()) {
                user.password = newPasswordText;
            }

            // TODO: Update the current user's profile/password in the database.
            profileName.setText(newUsername);
            newPassword.clear();
            saveMessage.setTextFill(Color.web(SUCCESS));
            saveMessage.setText("Changes saved.");
        });
        profileCard.getChildren().addAll(sectionTitle("Profile"), profileHeader, username, newPassword, saveMessage, save);

        VBox appearanceCard = card();
        Label themeLabel = subtitle("Theme");
        HBox themeButtons = new HBox(10);
        Button lightTheme = currentTheme == Theme.LIGHT ? primaryButton("Light") : secondaryButton("Light");
        Button darkTheme = currentTheme == Theme.DARK ? primaryButton("Dark") : secondaryButton("Dark");
        lightTheme.setGraphic(icon("fas-sun", 13, currentTheme == Theme.LIGHT ? "#FFFFFF" : primaryText()));
        darkTheme.setGraphic(icon("fas-moon", 13, currentTheme == Theme.DARK ? "#FFFFFF" : primaryText()));
        lightTheme.setOnAction(event -> applyTheme(stage, Theme.LIGHT, Page.SETTINGS));
        darkTheme.setOnAction(event -> applyTheme(stage, Theme.DARK, Page.SETTINGS));
        themeButtons.getChildren().addAll(lightTheme, darkTheme);
        appearanceCard.getChildren().addAll(sectionTitle("Appearance"), themeLabel, themeButtons);

        Button logout = dangerButton("Logout");
        logout.setGraphic(icon("fas-sign-out-alt", 13, "#FFFFFF"));
        logout.setMaxWidth(155);
        logout.setOnAction(event -> logout(stage));

        main.getChildren().addAll(heading, subheading, profileCard, appearanceCard, logout);
        root.setCenter(wrapScroll(main));
        return new Scene(root);
    }

    private void showPage(Stage stage, Page pageToShow) {
        boolean wasMaximized = stage.isMaximized();
        boolean wasFullScreen = stage.isFullScreen();
        double width = stage.getWidth();
        double height = stage.getHeight();

        buildScenes(stage);
        stage.setScene(sceneFor(pageToShow));

        if (!wasMaximized && !wasFullScreen) {
            stage.setWidth(width);
            stage.setHeight(height);
            stage.centerOnScreen();
        }

        Platform.runLater(() -> {
            if (wasFullScreen) {
                stage.setFullScreen(true);
            }
            if (wasMaximized) {
                stage.setMaximized(true);
            }
        });
    }

    private void applyTheme(Stage stage, Theme theme, Page pageToShow) {
        currentTheme = theme;
        showPage(stage, pageToShow);
    }

    private void logout(Stage stage) {
        currentUser = null;
        currentTaskFilter = TaskFilter.ALL;
        showPage(stage, Page.LOGIN);
    }

    private void resizeStage(Stage stage, double width, double height) {
        stage.setMaximized(false);
        stage.setFullScreen(false);
        stage.setWidth(width);
        stage.setHeight(height);
        stage.centerOnScreen();
    }

    private Scene sceneFor(Page page) {
        if (page == Page.LOGIN) {
            return loginScene;
        }
        if (page == Page.REGISTER) {
            return registerScene;
        }
        if (page == Page.TASKS) {
            return tasksScene;
        }
        if (page == Page.CALENDAR) {
            return calendarScene;
        }
        if (page == Page.SETTINGS) {
            return settingsScene;
        }
        if (page == Page.WELCOME) {
            return welcomeScene;
        }
        return homeScene;
    }

    private BorderPane appShell(Stage stage, Page activePage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + bgColor() + ";");
        root.setLeft(sidebar(stage, activePage));
        return root;
    }

    private StackPane centeredPage() {
        StackPane root = new StackPane();
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: " + appGradient() + ";");
        return root;
    }

    private VBox mainContent() {
        VBox main = new VBox(20);
        main.setPadding(new Insets(34, 38, 34, 38));
        main.setStyle("-fx-background-color: " + bgColor() + ";");
        return main;
    }

    private ScrollPane wrapScroll(VBox content) {
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: " + bgColor() + "; -fx-background-color: transparent;");
        return scroll;
    }

    private StackPane welcomeIllustration() {
        StackPane art = new StackPane();
        art.setMinSize(360, 210);
        art.setMaxSize(360, 210);

        Rectangle backBlob = new Rectangle(290, 160);
        backBlob.setArcWidth(54);
        backBlob.setArcHeight(54);
        backBlob.setFill(Color.web(currentTheme == Theme.DARK ? "#1E293B" : "#E9E7FF"));

        Rectangle laptop = new Rectangle(240, 132);
        laptop.setArcWidth(28);
        laptop.setArcHeight(28);
        laptop.setFill(Color.web(surfaceColor()));
        laptop.setStroke(Color.web(borderColor()));
        laptop.setEffect(shadow(24, 0.16));

        VBox checklist = new VBox(11);
        checklist.setMaxWidth(185);
        checklist.setAlignment(Pos.CENTER_LEFT);
        checklist.getChildren().addAll(
                illustrationLine("fas-check-circle", "Plan project screens"),
                illustrationLine("fas-check-circle", "Design task cards"),
                illustrationLine("fas-circle", "Connect database")
        );

        Rectangle base = new Rectangle(275, 16);
        base.setArcWidth(20);
        base.setArcHeight(20);
        base.setFill(Color.web(currentTheme == Theme.DARK ? "#334155" : "#CBD5E1"));
        StackPane.setAlignment(base, Pos.BOTTOM_CENTER);

        Circle dot = new Circle(18, Color.web(PRIMARY));
        StackPane.setAlignment(dot, Pos.TOP_RIGHT);
        StackPane.setMargin(dot, new Insets(18, 34, 0, 0));

        art.getChildren().addAll(backBlob, laptop, checklist, base, dot);
        return art;
    }

    private HBox illustrationLine(String iconCode, String text) {
        Label textLabel = new Label(text);
        textLabel.setTextFill(Color.web(textColor()));
        textLabel.setFont(Font.font("Verdana", 12));
        HBox line = new HBox(8, icon(iconCode, 14, PRIMARY), textLabel);
        line.setAlignment(Pos.CENTER_LEFT);
        return line;
    }

    private Button primaryButton(String text) {
        Button button = baseButton(text);
        button.setTextFill(Color.WHITE);
        installButtonStyles(
                button,
                "-fx-background-color: " + PRIMARY + "; -fx-background-radius: 13; -fx-padding: 11 18;",
                "-fx-background-color: " + PRIMARY_DARK + "; -fx-background-radius: 13; -fx-padding: 11 18;"
        );
        button.setEffect(new DropShadow(12, Color.rgb(109, 93, 246, 0.22)));
        return button;
    }

    private Button secondaryButton(String text) {
        Button button = baseButton(text);
        button.setTextFill(Color.web(primaryText()));
        installButtonStyles(
                button,
                "-fx-background-color: " + surfaceAltColor() + "; -fx-background-radius: 13; -fx-padding: 11 18; -fx-border-color: " + borderColor() + "; -fx-border-radius: 13;",
                "-fx-background-color: " + surfaceAltColor() + "; -fx-background-radius: 13; -fx-padding: 11 18; -fx-border-color: " + PRIMARY + "; -fx-border-radius: 13;"
        );
        return button;
    }

    private Button dangerButton(String text) {
        Button button = baseButton(text);
        button.setTextFill(Color.WHITE);
        installButtonStyles(
                button,
                "-fx-background-color: " + DANGER + "; -fx-background-radius: 13; -fx-padding: 11 18;",
                "-fx-background-color: #DC2626; -fx-background-radius: 13; -fx-padding: 11 18;"
        );
        return button;
    }

    private Button sidebarButton(String text) {
        return sidebarButton(text, "fas-circle", false);
    }

    private Button sidebarButton(String text, String iconCode, boolean active) {
        Button button = new Button(text);
        button.setGraphic(icon(iconCode, 15, active ? "#FFFFFF" : "#94A3B8"));
        button.setGraphicTextGap(12);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setMinHeight(46);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setTextFill(Color.web(active ? "#FFFFFF" : "#CBD5E1"));
        button.setFont(Font.font("Verdana", FontWeight.BOLD, 13));
        button.setCursor(Cursor.HAND);

        String normal = active ? activeNavStyle() : navStyle("transparent");
        String hover = active ? activeNavStyle() : navStyle("#1E293B");
        installButtonStyles(button, normal, hover);
        if (active) {
            button.setEffect(new DropShadow(15, Color.rgb(109, 93, 246, 0.24)));
        }
        return button;
    }

    private Label title(String text) {
        Label label = new Label(text);
        label.setTextFill(Color.web(textColor()));
        label.setFont(Font.font("Verdana", FontWeight.BOLD, 28));
        label.setWrapText(true);
        return label;
    }

    private Label subtitle(String text) {
        Label label = new Label(text);
        label.setTextFill(Color.web(mutedColor()));
        label.setFont(Font.font("Verdana", 14));
        label.setWrapText(true);
        return label;
    }

    private TextField input(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setPrefHeight(45);
        field.setStyle(inputStyle());
        return field;
    }

    private PasswordField passwordInput(String prompt) {
        PasswordField field = new PasswordField();
        field.setPromptText(prompt);
        field.setPrefHeight(45);
        field.setStyle(inputStyle());
        return field;
    }

    private VBox card() {
        VBox box = new VBox(16);
        box.setPadding(new Insets(24));
        box.setStyle(cardStyle());
        box.setEffect(shadow(24, 0.10));
        return box;
    }

    private VBox statCard(String title, String value) {
        return statCard(title, value, "fas-chart-bar", PRIMARY);
    }

    private VBox statCard(String title, String value, String iconCode, String accent) {
        VBox box = card();
        box.setMinWidth(180);
        box.setMinHeight(145);
        HBox top = new HBox();
        top.setAlignment(Pos.TOP_CENTER);
        Label titleLabel = subtitle(title);
        StackPane iconBox = iconBubble(iconCode, accent);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        top.getChildren().addAll(titleLabel, spacer, iconBox);

        Label valueLabel = new Label(value);
        valueLabel.setTextFill(Color.web(textColor()));
        valueLabel.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 34));
        Label hint = new Label("Live account data");
        hint.setTextFill(Color.web(mutedColor()));
        hint.setFont(Font.font("Verdana", 12));
        box.getChildren().addAll(top, valueLabel, hint);
        return box;
    }

    private HBox taskItem(Task task, Stage stage) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(15));
        row.setStyle("-fx-background-color: " + surfaceAltColor() + "; -fx-background-radius: 17; -fx-border-color: " + borderColor() + "; -fx-border-radius: 17;");

        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(task.completed);

        Label taskTitle = new Label(task.title);
        taskTitle.setTextFill(Color.web(task.completed ? SUCCESS : textColor()));
        taskTitle.setFont(Font.font("Verdana", FontWeight.BOLD, 13));
        taskTitle.setStyle(task.completed ? "-fx-strikethrough: true;" : "");

        Label priorityLabel = pill(task.priority, priorityColor(task.priority), priorityBackground(task.priority));
        Label deadlineLabel = subtitle(formatDeadline(task.deadline));
        deadlineLabel.setMinWidth(95);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button edit = secondaryButton("Edit");
        edit.setGraphic(icon("fas-edit", 12, primaryText()));
        edit.setOnAction(event -> {
            TextInputDialog dialog = new TextInputDialog(task.title);
            dialog.setTitle("Edit Task");
            dialog.setHeaderText("Update task title");
            dialog.setContentText("Task:");
            dialog.showAndWait().ifPresent(newTitle -> {
                String cleanedTitle = newTitle.trim();
                if (!cleanedTitle.isEmpty()) {
                    task.title = cleanedTitle;
                    // TODO: Update this task in the database using task.id and currentUser.email.
                    showPage(stage, Page.TASKS);
                }
            });
        });

        Button delete = dangerButton("Delete");
        delete.setGraphic(icon("fas-trash-alt", 12, "#FFFFFF"));
        delete.setOnAction(event -> {
            activeUser().tasks.removeIf(savedTask -> savedTask.id == task.id);
            // TODO: Delete this task from the database using task.id and currentUser.email.
            showPage(stage, Page.TASKS);
        });

        checkBox.selectedProperty().addListener((observable, oldValue, selected) -> {
            task.completed = selected;
            // TODO: Update the completed status in the database using task.id and currentUser.email.
            showPage(stage, Page.TASKS);
        });

        row.getChildren().addAll(checkBox, taskTitle, spacer, priorityLabel, deadlineLabel, edit, delete);
        return row;
    }

    private void refreshTaskListView(Stage stage, User user) {
        if (taskListView == null) {
            return;
        }

        List<Task> visibleTasks = filteredTasks(user);
        observableTasks.setAll(visibleTasks);
        taskListView.setPlaceholder(emptyState("No tasks in this view yet."));
        taskListView.refresh();
    }

    private VBox sidebar(Stage stage) {
        return sidebar(stage, Page.DASHBOARD);
    }

    private VBox sidebar(Stage stage, Page activePage) {
        VBox sidebar = new VBox(12);
        sidebar.setPadding(new Insets(28, 18, 20, 18));
        sidebar.setPrefWidth(242);
        sidebar.setStyle("-fx-background-color: " + sidebarColor() + ";");

        HBox brand = new HBox(11);
        brand.setAlignment(Pos.CENTER_LEFT);
        Label mark = logoMark(44, 14);
        Label name = new Label("FocusPad");
        name.setTextFill(Color.WHITE);
        name.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 20));
        brand.getChildren().addAll(mark, name);

        Button dashboard = sidebarButton("Dashboard", "fas-home", activePage == Page.DASHBOARD);
        Button tasks = sidebarButton("Tasks", "fas-tasks", activePage == Page.TASKS);
        Button calendar = sidebarButton("Calendar", "fas-calendar-alt", activePage == Page.CALENDAR);
        Button settings = sidebarButton("Settings", "fas-cog", activePage == Page.SETTINGS);
        Button logout = sidebarButton("Logout", "fas-sign-out-alt", false);

        dashboard.setOnAction(event -> showPage(stage, Page.DASHBOARD));
        tasks.setOnAction(event -> showPage(stage, Page.TASKS));
        calendar.setOnAction(event -> showPage(stage, Page.CALENDAR));
        settings.setOnAction(event -> showPage(stage, Page.SETTINGS));
        logout.setOnAction(event -> logout(stage));

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        Label footer = new Label("Study smarter, one task at a time.");
        footer.setTextFill(Color.web("#64748B"));
        footer.setFont(Font.font("Verdana", 11));
        footer.setWrapText(true);

        sidebar.getChildren().addAll(brand, navDivider(), dashboard, tasks, calendar, settings, spacer, footer, logout);
        return sidebar;
    }

    private Button baseButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Verdana", FontWeight.BOLD, 13));
        button.setCursor(Cursor.HAND);
        button.setMinHeight(42);
        button.setGraphicTextGap(8);
        return button;
    }

    private Button linkButton(String text) {
        Button button = new Button(text);
        button.setTextFill(Color.web(primaryText()));
        button.setFont(Font.font("Verdana", FontWeight.BOLD, 13));
        installButtonStyles(
                button,
                "-fx-background-color: transparent; -fx-padding: 8 0;",
                "-fx-background-color: transparent; -fx-padding: 8 0; -fx-underline: true;"
        );
        button.setCursor(Cursor.HAND);
        return button;
    }

    private Button filterButton(String text, boolean active) {
        Button button = baseButton(text);
        if (active) {
            button.setTextFill(Color.WHITE);
            installButtonStyles(button, pillButtonStyle(PRIMARY, PRIMARY), pillButtonStyle(PRIMARY_DARK, PRIMARY_DARK));
        } else {
            button.setTextFill(Color.web(mutedColor()));
            installButtonStyles(
                    button,
                    pillButtonStyle(surfaceColor(), borderColor()),
                    pillButtonStyle(surfaceAltColor(), PRIMARY)
            );
        }
        return button;
    }

    private String inputStyle() {
        return "-fx-background-color: " + surfaceColor() + "; "
                + "-fx-text-fill: " + textColor() + "; "
                + "-fx-prompt-text-fill: " + mutedColor() + "; "
                + "-fx-background-radius: 13; "
                + "-fx-border-color: " + borderColor() + "; "
                + "-fx-border-radius: 13; "
                + "-fx-padding: 10 14; "
                + "-fx-font-family: Verdana; "
                + "-fx-font-size: 13;";
    }

    private HBox miniTask(String text, boolean complete) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(13));
        row.setStyle("-fx-background-color: " + surfaceAltColor() + "; -fx-background-radius: 15;");
        Label label = new Label(text);
        label.setTextFill(Color.web(textColor()));
        label.setFont(Font.font("Verdana", FontWeight.BOLD, 13));
        row.getChildren().addAll(icon(complete ? "fas-check-circle" : "fas-circle", 15, complete ? SUCCESS : PRIMARY), label);
        return row;
    }

    private Label pill(String text, String color, String background) {
        Label label = new Label(text);
        label.setTextFill(Color.web(color));
        label.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
        label.setPadding(new Insets(6, 10, 6, 10));
        label.setStyle("-fx-background-color: " + background + "; -fx-background-radius: 999;");
        return label;
    }

    private String priorityColor(String priority) {
        if ("High".equals(priority)) {
            return "#DC2626";
        }
        if ("Medium".equals(priority)) {
            return "#D97706";
        }
        return "#16A34A";
    }

    private String priorityBackground(String priority) {
        if ("High".equals(priority)) {
            return currentTheme == Theme.DARK ? "#3B1218" : "#FEE2E2";
        }
        if ("Medium".equals(priority)) {
            return currentTheme == Theme.DARK ? "#3B2A0A" : "#FEF3C7";
        }
        return currentTheme == Theme.DARK ? "#0F2F1D" : "#DCFCE7";
    }

    private Label upcomingItem(String title, String time) {
        Label label = new Label(title + "  -  " + time);
        label.setTextFill(Color.web(textColor()));
        label.setFont(Font.font("Verdana", FontWeight.BOLD, 13));
        label.setPadding(new Insets(13));
        label.setMaxWidth(Double.MAX_VALUE);
        label.setStyle("-fx-background-color: " + surfaceAltColor() + "; -fx-background-radius: 15; -fx-border-color: " + borderColor() + "; -fx-border-radius: 15;");
        return label;
    }

    private GridPane monthGrid(LocalDate monthAnchor, LocalDate selectedDate) {
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(8, 0, 0, 0));

        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (int i = 0; i < days.length; i++) {
            grid.add(calendarCell(days[i], true, false), i, 0);
        }

        LocalDate firstOfMonth = monthAnchor.withDayOfMonth(1);
        int startOffset = firstOfMonth.getDayOfWeek().getValue() - 1;
        int daysInMonth = firstOfMonth.lengthOfMonth();
        int day = 1;

        for (int cell = 0; cell < 35; cell++) {
            int row = (cell / 7) + 1;
            int col = cell % 7;
            if (cell < startOffset || day > daysInMonth) {
                grid.add(calendarCell("", false, false), col, row);
                continue;
            }

            LocalDate current = firstOfMonth.withDayOfMonth(day);
            boolean highlighted = selectedDate != null && current.equals(selectedDate);
            grid.add(calendarCell(String.valueOf(day), false, highlighted), col, row);
            day++;
        }

        return grid;
    }

    private Label calendarCell(String text, boolean header, boolean highlighted) {
        Label label = new Label(text);
        label.setAlignment(Pos.CENTER);
        label.setTextAlignment(TextAlignment.CENTER);
        label.setMinSize(54, 42);
        label.setFont(Font.font("Verdana", header ? FontWeight.BOLD : FontWeight.NORMAL, 12));

        if (header) {
            label.setTextFill(Color.web(mutedColor()));
        } else if (highlighted) {
            label.setTextFill(Color.WHITE);
            label.setStyle("-fx-background-color: " + PRIMARY + "; -fx-background-radius: 12;");
        } else {
            label.setTextFill(Color.web(textColor()));
            label.setStyle("-fx-background-color: " + surfaceAltColor() + "; -fx-background-radius: 12;");
        }

        return label;
    }

    private String formatMonthYear(LocalDate date) {
        return date.getMonth().name().substring(0, 1) + date.getMonth().name().substring(1).toLowerCase() + " " + date.getYear();
    }

    private StackPane avatar(String initials) {
        Circle circle = new Circle(28, Color.web(surfaceAltColor()));
        Label label = new Label(initials);
        label.setTextFill(Color.web(primaryText()));
        label.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 18));
        return new StackPane(circle, label);
    }

    private void setCircularAvatarImage(ImageView target, Image image) {
        if (image == null || image.isError()) {
            return;
        }

        target.setImage(image);

        double width = image.getWidth();
        double height = image.getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }

        double square = Math.min(width, height);
        double x = (width - square) / 2.0;
        double y = (height - square) / 2.0;
        target.setViewport(new Rectangle2D(x, y, square, square));
    }

    private Label logoMark(double size, int fontSize) {
        Label mark = new Label("FP");
        mark.setAlignment(Pos.CENTER);
        mark.setTextFill(Color.WHITE);
        mark.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, fontSize));
        mark.setMinSize(size, size);
        mark.setMaxSize(size, size);
        mark.setStyle("-fx-background-color: linear-gradient(to bottom right, " + PRIMARY + ", #9B8CFF); -fx-background-radius: " + (size / 3) + ";");
        mark.setEffect(new DropShadow(16, Color.rgb(109, 93, 246, 0.28)));
        return mark;
    }

    private Label sectionTitle(String text) {
        Label label = title(text);
        label.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
        return label;
    }

    private Label bodyText(String text) {
        Label label = new Label(text);
        label.setTextFill(Color.web(textColor()));
        label.setFont(Font.font("Verdana", 14));
        label.setWrapText(true);
        return label;
    }

    private Label errorLabel() {
        Label label = new Label("");
        label.setTextFill(Color.web(DANGER));
        label.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
        label.setMinHeight(16);
        return label;
    }

    private Label emptyState(String text) {
        Label label = subtitle(text);
        label.setPadding(new Insets(18));
        label.setMaxWidth(Double.MAX_VALUE);
        label.setStyle("-fx-background-color: " + surfaceAltColor() + "; -fx-background-radius: 16; -fx-border-color: " + borderColor() + "; -fx-border-radius: 16;");
        return label;
    }

    private HBox chip(String iconCode, String text) {
        HBox chip = new HBox(8);
        chip.setAlignment(Pos.CENTER);
        chip.setPadding(new Insets(10, 14, 10, 14));
        chip.setStyle("-fx-background-color: " + surfaceColor() + "; -fx-background-radius: 999; -fx-border-color: " + borderColor() + "; -fx-border-radius: 999;");
        Label label = new Label(text);
        label.setTextFill(Color.web(textColor()));
        label.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
        chip.getChildren().addAll(icon(iconCode, 13, PRIMARY), label);
        return chip;
    }

    private HBox actionRow(String iconCode, String title, String text) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10));
        row.setCursor(Cursor.HAND);
        row.setStyle("-fx-background-color: " + surfaceAltColor() + "; -fx-background-radius: 15;");
        VBox labels = new VBox(3);
        Label actionTitle = new Label(title);
        actionTitle.setTextFill(Color.web(textColor()));
        actionTitle.setFont(Font.font("Verdana", FontWeight.BOLD, 13));
        Label actionText = new Label(text);
        actionText.setTextFill(Color.web(mutedColor()));
        actionText.setFont(Font.font("Verdana", 11));
        labels.getChildren().addAll(actionTitle, actionText);
        row.getChildren().addAll(iconBubble(iconCode, PRIMARY), labels);
        return row;
    }

    private StackPane iconBubble(String iconCode, String accent) {
        StackPane bubble = new StackPane();
        bubble.setMinSize(42, 42);
        bubble.setMaxSize(42, 42);
        bubble.setStyle("-fx-background-color: " + accentSoft(accent) + "; -fx-background-radius: 14;");
        bubble.getChildren().add(icon(iconCode, 16, accent));
        return bubble;
    }

    private FontIcon icon(String iconCode, int size, String color) {
        FontIcon icon = new FontIcon(iconCode);
        icon.setIconSize(size);
        icon.setIconColor(Color.web(color));
        return icon;
    }

    private void installButtonStyles(Button button, String normalStyle, String hoverStyle) {
        button.setStyle(normalStyle);
        button.setOnMouseEntered(event -> button.setStyle(hoverStyle));
        button.setOnMouseExited(event -> button.setStyle(normalStyle));
    }

    private DropShadow shadow(double radius, double opacity) {
        return new DropShadow(radius, Color.rgb(15, 23, 42, currentTheme == Theme.DARK ? opacity + 0.10 : opacity));
    }

    private Region navDivider() {
        Region divider = new Region();
        divider.setMinHeight(18);
        return divider;
    }

    private User activeUser() {
        if (currentUser != null) {
            return currentUser;
        }
        return usersByEmail.get("bashar@example.com");
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private boolean isValidEmail(String email) {
        String trimmed = email.trim();
        return trimmed.contains("@") && trimmed.contains(".") && trimmed.indexOf("@") > 0 && trimmed.lastIndexOf(".") > trimmed.indexOf("@") + 1;
    }

    private int completedCount(User user) {
        int count = 0;
        for (Task task : user.tasks) {
            if (task.completed) {
                count++;
            }
        }
        return count;
    }

    private int pendingCount(User user) {
        return user.tasks.size() - completedCount(user);
    }

    private List<Task> filteredTasks(User user) {
        List<Task> results = new ArrayList<>();
        for (Task task : user.tasks) {
            if (currentTaskFilter == TaskFilter.ALL
                    || (currentTaskFilter == TaskFilter.COMPLETED && task.completed)
                    || (currentTaskFilter == TaskFilter.PENDING && !task.completed)) {
                results.add(task);
            }
        }
        return results;
    }

    private Task firstPendingTask(User user) {
        for (Task task : user.tasks) {
            if (!task.completed) {
                return task;
            }
        }
        return null;
    }

    private List<Task> recentTasks(User user, int limit) {
        List<Task> recent = new ArrayList<>();
        for (int i = user.tasks.size() - 1; i >= 0 && recent.size() < limit; i--) {
            recent.add(user.tasks.get(i));
        }
        return recent;
    }

    private String firstName(String username) {
        String trimmed = username.trim();
        if (trimmed.isEmpty()) {
            return "User";
        }
        int spaceIndex = trimmed.indexOf(" ");
        return spaceIndex == -1 ? trimmed : trimmed.substring(0, spaceIndex);
    }

    private String formatDeadline(LocalDate deadline) {
        return deadline == null ? "No deadline" : deadline.format(dateFormatter);
    }

    private String listViewStyle() {
        return "-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;";
    }

    private String cardStyle() {
        return "-fx-background-color: " + surfaceColor() + "; -fx-background-radius: 24; -fx-border-color: " + borderColor() + "; -fx-border-radius: 24;";
    }

    private String panelStyle() {
        return "-fx-background-color: " + surfaceColor() + "; -fx-background-radius: 22; -fx-border-color: " + borderColor() + "; -fx-border-radius: 22;";
    }

    private String navStyle(String color) {
        return "-fx-background-color: " + color + "; -fx-background-radius: 14; -fx-padding: 12 16;";
    }

    private String activeNavStyle() {
        return "-fx-background-color: linear-gradient(to right, " + PRIMARY + ", " + PRIMARY_DARK + "); -fx-background-radius: 14; -fx-padding: 12 16;";
    }

    private String pillButtonStyle(String background, String border) {
        return "-fx-background-color: " + background + "; -fx-background-radius: 999; -fx-padding: 9 18; -fx-border-color: " + border + "; -fx-border-radius: 999;";
    }

    private String appGradient() {
        if (currentTheme == Theme.DARK) {
            return "linear-gradient(to bottom right, #020617, #111827)";
        }
        return "linear-gradient(to bottom right, #F7F7FB, #EEEAFE)";
    }

    private String bgColor() {
        if (currentTheme == Theme.DARK) {
            return "#0B1120";
        }
        return "#F7F7FB";
    }

    private String surfaceColor() {
        if (currentTheme == Theme.DARK) {
            return "#111827";
        }
        return "#FFFFFF";
    }

    private String surfaceAltColor() {
        if (currentTheme == Theme.DARK) {
            return "#1E293B";
        }
        return "#F8FAFC";
    }

    private String sidebarColor() {
        if (currentTheme == Theme.DARK) {
            return "#020617";
        }
        return "#0A1020";
    }

    private String textColor() {
        if (currentTheme == Theme.DARK) {
            return "#F8FAFC";
        }
        return "#111827";
    }

    private String mutedColor() {
        if (currentTheme == Theme.DARK) {
            return "#94A3B8";
        }
        return "#6B7280";
    }

    private String borderColor() {
        if (currentTheme == Theme.DARK) {
            return "#263244";
        }
        return "#E5E7EB";
    }

    private String primaryText() {
        return currentTheme == Theme.DARK ? "#AFA7FF" : PRIMARY_DARK;
    }

    private String accentSoft(String accent) {
        if (currentTheme == Theme.DARK) {
            return "#1E293B";
        }
        if (SUCCESS.equals(accent)) {
            return "#DCFCE7";
        }
        if (WARNING.equals(accent)) {
            return "#FEF3C7";
        }
        return "#EFEEFF";
    }

    public static void main(String[] args) {
        launch(args);
    }
}
