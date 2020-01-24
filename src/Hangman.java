/*

Prashun Dey

Hangman GUI

    Created with single file because of coursework requirement
    Could've obviously been more modular

*/

import javafx.application.Application;
import java.io.FileNotFoundException;
import java.util.*;
import java.io.File;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import java.util.ArrayList;
import javafx.scene.image.Image;

import javafx.stage.FileChooser;
import java.io.PrintWriter;
import java.io.IOException;
import java.lang.Exception;


public class Hangman extends Application {

    // Game Status Helpers
    private boolean won = false;
    private boolean lost = false;
    private int guessLeft = 10;

    // Variables Dealing with word status
    private ArrayList<String> collection = new ArrayList<>();
    private String word = "";
    private int lettersToGo = 0;

    // Array Lists of ALL Lettered Boxes on Screen
    private ArrayList<Button> digitalKeyBoard = new ArrayList<>(26);
    private ArrayList<Button> displayedLetters = new ArrayList<>(45);

    // Main Toolbar Buttons
    private Button newGameBtn = new Button();
    private Button saveBtn = new Button();
    private Button loadBtn = new Button();
    private Button quitBtn = new Button();
    private Button startBtn = new Button("Start Playing");

    // Helper Variables for ToolBar
    private boolean gameInProgress = false;
    private boolean gameChanged = false;


    // Guesses Remaining Statement
    private Text remainder = new Text("Remaining Guesses: " + guessLeft);

    // HANGMAN Body Parts
    private Line part1 = new Line(250,300,0,300);
    private Line part2 = new Line(0,300,0,0);
    private Line part3 = new Line(0,0,150,0);
    private Line part4 = new Line(150,0,150,50);
    private Circle part5 = new Circle(150, 80, 30);
    private Line part6 = new Line(150,110,150,180);
    private Line part7 = new Line(150,130,115,120);
    private Line part8 = new Line(150,130,185,120);
    private Line part9 = new Line(150,180,130,230);
    private Line part10 = new Line(150,180,170,230);

    // GUI Main Components
    private BorderPane whole = new BorderPane();
    private Scene scene = new Scene(whole);

    // Keyboard Event Handlers
    private EventHandler<KeyEvent> keyBoardEventHandler = e -> {
        String key = e.getCode().toString();
        if(key.length() == 1) {
            char c = key.charAt(0);
            handleValidInput(c);
        }
    };

    private EventHandler<MouseEvent> screenKeysEventHandler = event ->  {
        Button b = (Button) event.getSource();
        char letterSelected = b.getText().charAt(0);
        handleValidInput(letterSelected);
    };




    @Override
    public void start(Stage primaryStage) throws Exception{

        HBox header = createInitialHeader();
        VBox gameStarted = new VBox();
        HBox footer = createFooter();

        whole.setBackground(new Background(new BackgroundFill(Color.rgb(190, 230, 255), CornerRadii.EMPTY, Insets.EMPTY)));
        whole.setTop(header);
        whole.setCenter(gameStarted);
        whole.setBottom(footer);
        footer.setVisible(false);

        primaryStage.setScene(scene);
        setPrimaryStage(primaryStage);
        primaryStage.show();

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter(".hng", "*.hng"));

        newGameBtn.setOnAction(event -> {
            if(!won && !lost && gameChanged) {
                askToSaveOrNewGameStart();
            }

            else{
                whole.getCenter().setVisible(false);
                footer.setVisible(true);
                startBtn.setDisable(false);
            }
            gameInProgress = false;
        });


        startBtn.setOnAction(event -> {
            startNewGame(whole, false);
            startBtn.setDisable(true);
            gameInProgress= true;
        });


        saveBtn.setOnAction(event -> {
            File file = fileChooser.showSaveDialog(primaryStage);

            if (file != null) {
                save(gatherData(), file);
                saveBtn.setDisable(true);
            }
        });


        loadBtn.setOnAction(event -> {
            if (!won && !lost && gameChanged) {
                askToSaveOrNewGameStart();
            }

            File file = fileChooser.showOpenDialog(primaryStage);

            if (file != null) {
                try {
                    String name = file.getName();
                    String fileExtension = name.substring(name.lastIndexOf(".") + 1, file.getName().length());
                    if (fileExtension.equals("hng")) {
                        resetGame();
                        resetHangMan();
                        load(file);
                    }
                    else {
                        Alert error = new Alert(Alert.AlertType.ERROR, "." + fileExtension + " not valid selection\nAllowed only(.hng)");
                        error.show();
                    }
                } catch (FileNotFoundException e){
                    Alert error = new Alert(Alert.AlertType.ERROR, "Not Able to Load File");
                    error.show();
                }
            }
        });


        quitBtn.setOnAction(event -> {
            if(!won && !lost && gameChanged) {
                askToSaveOrQuit(primaryStage);
            }
            else{
                primaryStage.close();
            }
        });
    }



    private void askToSaveOrNewGameStart() {
        System.out.println("POPUP -> askToSaveOrNewGameStart");
        Stage popup = new Stage();

        VBox all_box = new VBox(new Text("Would You Like to Save the Game"));
        HBox button_box = new HBox();
        Button yes = new Button("Yes");
        Button no = new Button("No");
        Button cancel = new Button("Cancel");
        button_box.setSpacing(15);
        button_box.getChildren().addAll(yes, no, cancel);
        button_box.setAlignment(Pos.CENTER);
        all_box.getChildren().add(button_box);
        all_box.setSpacing(10);
        all_box.setAlignment(Pos.CENTER);

        yes.setOnAction(event -> {
            saveBtn.fire();
            popup.close();
            whole.getCenter().setVisible(false);
        });

        no.setOnAction(event -> {
            popup.close();
            whole.getCenter().setVisible(false);
            startBtn.setDisable(false);
        });

        cancel.setOnAction(event -> popup.close());

        Scene newScene = new Scene(all_box);
        popup.setScene(newScene);
        popup.setWidth(300);
        popup.setHeight(200);
        popup.show();
    }


    private void askToSaveOrQuit(Stage primaryStage) {
        System.out.println("POPUP -> askToSaveOrQuit");
        Stage popup = new Stage();

        VBox all_box = new VBox(new Text("Would You Like to Save the Game"));
        HBox button_box = new HBox();
        Button yes = new Button("Yes");
        Button no = new Button("No");
        Button cancel = new Button("Cancel");
        button_box.setSpacing(15);
        button_box.getChildren().addAll(yes, no, cancel);
        button_box.setAlignment(Pos.CENTER);
        all_box.getChildren().add(button_box);
        all_box.setSpacing(10);
        all_box.setAlignment(Pos.CENTER);

        yes.setOnAction(event -> {
            saveBtn.fire();
            popup.close(); // If file chooser is exited through window/OS exit
            primaryStage.close();
        });

        no.setOnAction(event -> {
            popup.close();
            primaryStage.close();
        });

        cancel.setOnAction(event -> popup.close());

        Scene newScene = new Scene(all_box);
        popup.setScene(newScene);
        popup.setWidth(300);
        popup.setHeight(200);
        popup.show();
    }


    private void load(File file) throws FileNotFoundException {
        Scanner scanner = new Scanner(file);
        String loadedWord;
        ArrayList<Integer> loadedDisabledChars = new ArrayList<>();

        loadedWord = scanner.nextLine();
        while (scanner.hasNext()) {
            String string = scanner.nextLine();
            if (string.length() == 1)
                loadedDisabledChars.add((int) string.charAt(0));
        }

        word = loadedWord;
        lettersToGo = loadedWord.length();

        whole.setCenter(createGame());

        for(int i: loadedDisabledChars){
            handleValidInput((char) i);
        }

        // Even though game not changed, load replays the game, triggering the game status to made at least 1 move
        gameChanged = false;
        saveBtn.setDisable(true);
    }


    private void save(ArrayList<String> content, File file) {
        try {
            PrintWriter transfer = new PrintWriter(file);
            for(String s: content)
                transfer.println(s);
            transfer.close();
        } catch (IOException ex) {
            System.out.println("Error saving -> while writing onto File");
        }
    }


    private ArrayList<String> gatherData() {
        ArrayList<String> data = new ArrayList<>();
        data.add(word);
        // Use characters already inputted to "REPLAY" the game
        for(Button b: digitalKeyBoard) {
            if(b.isDisable())
                data.add(b.getText());
        }
        return data;
    }


    private void setPrimaryStage(Stage primaryStage) {
        primaryStage.setHeight(600);
        primaryStage.setWidth(1200);
        primaryStage.setMinHeight(500);
        primaryStage.setMinWidth(850);
        //Image img = new Image("/icon.png");
        Image img = new Image(getClass().getResource("icon.png").toString());
        //System.out.println("\nImage for TITLE Found w/ getClass().getResource(...):\n" + getClass().getResource("icon.png").toString());
        primaryStage.getIcons().add(img);
        primaryStage.setTitle("Hangman");
    }


    private HBox createInitialHeader() {
        // New Game Button
        Image img1 = new Image(getClass().getResource("newGameIcon.png").toString(),40,30,true,true);
        newGameBtn.setGraphic(new ImageView(img1));
        newGameBtn.setStyle("-fx-background-color: #43464B");
        newGameBtn.setMinWidth(45);
        newGameBtn.setPadding(new Insets(0.0f,0.0f,0.0f,0.0f));

        // Save Button
        Image img2 = new Image(getClass().getResource("saveIcon.png").toString(),40,28,true,true);
        saveBtn.setGraphic(new ImageView(img2));
        saveBtn.setStyle("-fx-background-color: #43464B;");
        saveBtn.setMinWidth(45);
        saveBtn.setPadding(new Insets(2.0f,0.0f,0.0f,0.0f));

        // Load Button
        Image img3 = new Image(getClass().getResource("loadIcon.png").toString(),40,30,true,true);
        loadBtn.setGraphic(new ImageView(img3));
        loadBtn.setStyle("-fx-background-color: #43464B;");
        loadBtn.setMinWidth(45);
        loadBtn.setPadding(new Insets(0.0f,0.0f,0.0f,0.0f));

        // Quit Button
        Image img4 = new Image(getClass().getResource("quitIcon.png").toString(),40,30,true,true);
        quitBtn.setGraphic(new ImageView(img4));
        quitBtn.setStyle("-fx-background-color: #43464B;");
        quitBtn.setMinWidth(45);
        quitBtn.setPadding(new Insets(0.0f,0.0f,0.0f,0.0f));

        HBox header = new HBox(10);
        header.setMinHeight(45);
        header.setMaxHeight(45);
        header.setSpacing(10);
        header.setPadding(new Insets(0.0f,0.0f,0.0f,10.0f));
        header.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().addAll(newGameBtn, saveBtn, loadBtn, quitBtn);
        header.setStyle("-fx-background-color: #090E22");

        saveBtn.setDisable(true);

        return header;
    }


    private HBox createFooter() {
        startBtn.setStyle("-fx-font: 15 arial; -fx-base: #b6e7c9;");

        HBox footer = new HBox();
        footer.setStyle("-fx-background-color: #090E22");
        footer.setMinHeight(40);
        footer.setAlignment(Pos.CENTER);
        footer.getChildren().add(startBtn);
        return footer;
    }


    private void chooseWordFromFile() throws FileNotFoundException {
        Random random = new Random();
        //Scanner scanner = new Scanner(new File("/words.txt"));
        //Scanner scanner = new Scanner(new File("/Users/prashundey/IdeaProjects/111619351_HW3/src/sample/words.txt"));
        Scanner scanner = new Scanner(new File(getClass().getResource("words.txt").getFile()));
        //System.out.println("\nFile Found w/ getClass().getResource(...):\n" + getClass().getResource("words.txt").toString());

        while(scanner.hasNext())
            collection.add(scanner.nextLine());

        int randomIndex = random.nextInt(collection.size());
        word = collection.get(randomIndex).toUpperCase();
        lettersToGo = word.length();
    }

    private void userPicksWord() {
        Stage popup = new Stage();
        popup.setTitle("words.txt not found");

        VBox all_box = new VBox();
        TextField entry = new TextField();
        entry.setPromptText("Enter A Word");
        entry.requestFocus();

        Label wordWarning = new Label("Enter a word to guess!");
        wordWarning.setDisable(true);

        HBox button_box = new HBox();
        Button setWord = new Button("Use Word");

        Button quit = new Button("Quit");
        button_box.setSpacing(15);
        button_box.getChildren().addAll(setWord, quit);
        button_box.setAlignment(Pos.CENTER);

        all_box.getChildren().addAll(entry, wordWarning, button_box);
        all_box.setSpacing(10);
        all_box.setAlignment(Pos.CENTER);


        setWord.setOnAction(event -> {
            System.out.println(entry.getText().length());
            if(!entry.getText().matches("[a-zA-Z]*") || entry.getText().length() < 3){
                entry.setStyle("-fx-text-fill: red;");
                wordWarning.setDisable(false);
            }

            else {
                setWord(entry.getText());
                popup.close();
            }

        });

        quit.setOnAction(event -> {
            popup.close();
        });


        Scene newScene = new Scene(all_box);
        popup.setScene(newScene);
        popup.setWidth(300);
        popup.setHeight(200);
        popup.showAndWait();

    }


    private void setWord(String word ) {
        this.word = word.toUpperCase();
        this.lettersToGo = word.length();
    }


    private VBox createGame() {
        gameInProgress = true;

        /*
        try{
            chooseWordFromFile();
        } catch (FileNotFoundException ex){
            System.out.println("\nFILE NOT FOUND EXCEPTION\nWord by default is set to HANGMAN");

            userPicksWord();
        } */

        // GAME TITLE
        Text name = new Text("HANGMAN");
        name.setStroke(Color.rgb(34,27,83));
        name.setStrokeWidth(4);
        name.setFill(Color.WHITE);
        name.setFont(Font.font ("Impact", FontWeight.SEMI_BOLD , 70));
        HBox title = new HBox();
        title.getChildren().addAll(name);
        title.setAlignment(Pos.CENTER);

        // GAME SECTION will include left side (hangman) and right side (remaining guess, blanks, keyboard)
        HBox gameSection = new HBox();
        gameSection.setAlignment(Pos.TOP_CENTER);

        // Hangman Section
        Group hangmanContainer = new Group();
        hangmanContainer.getChildren().addAll(part1, part2, part3, part4, part5, part6, part7, part8, part9, part10);

        // Created for visual padding purposes
        VBox leftSidePadder = new VBox();
        leftSidePadder.setMaxWidth(0);
        VBox middlePadder = new VBox();
        middlePadder.setMinWidth(50);

        // RIGHT SIDE will contain remaining guess, blanks, keyboard as vertical components
        VBox rightSide = new VBox();

        remainder.setFont(Font.font("Impact", FontWeight.LIGHT , 25));
        rightSide.setSpacing(50);
        rightSide.getChildren().addAll(remainder,createBlanksforWord(),createOnScreenKeyboard());

        gameSection.getChildren().addAll(leftSidePadder,hangmanContainer, middlePadder, rightSide);
        gameSection.setSpacing(50);

        // ALL SECTIONS PUT TOGETHER (title, game section)
        VBox v = new VBox(10);
        v.setBackground(new Background(new BackgroundFill(Color.rgb(190, 230, 255), CornerRadii.EMPTY, Insets.EMPTY)));
        //v.setStyle("-fx-background-color: linear-gradient(#d2dcd2, #333436);");
        v.getChildren().addAll(title,gameSection);

        return v;
    }


    private FlowPane createOnScreenKeyboard() {
        // RIGHT SIDE
        // Flow pane will hold on-screen keyboard
        FlowPane keys = new FlowPane();
        keys.setPrefWrapLength(500);
        keys.setHgap(5);
        keys.setVgap(5);

        // Create on-screen keyboard and then adding them to an array list for organized access
        int i = 0;
        for (char c = 'A'; c <= 'Z'; c++, i++) {
            Button b = new Button();
            b.setText(String.valueOf(c));
            b.setStyle("-fx-text-fill: white;");
            b.setStyle("-fx-background-color: #00ff00;");
            b.setMinHeight(40);
            b.setMinWidth(40);
            b.setPrefSize(40,40);
            keys.getChildren().add(b);
            digitalKeyBoard.add((Button) keys.getChildren().get(i));
        }

        assignEventHandlerKeyBoard();
        return keys;
    }


    private FlowPane createBlanksforWord() {
        // Guessed Letters Displayed using inactive Buttons
        FlowPane answerSoFar = new FlowPane();
        answerSoFar.setHgap(10);
        answerSoFar.setVgap(5);
        answerSoFar.setPrefWrapLength(800);
        for(int i = 0; i < word.length(); i++) {
            Button b = new Button();
            //b.setText(String.valueOf(word.charAt(i)));
            b.setStyle("-fx-text-fill: #FFFFFF");
            b.setStyle("-fx-background-color: #000000;");
            b.setMinHeight(40);
            b.setMinWidth(40);
            b.setDisable(true);
            b.setStyle("-fx-opacity: 1.0;");
            b.setPrefSize(40,40);
            answerSoFar.getChildren().add(b);
            displayedLetters.add((Button) answerSoFar.getChildren().get(i));
        }

        return answerSoFar;
    }


    private void assignEventHandlerKeyBoard() {
        for(Button b: digitalKeyBoard){
            b.addEventHandler(MouseEvent.MOUSE_CLICKED, screenKeysEventHandler);
        }

        scene.addEventHandler(KeyEvent.KEY_PRESSED, keyBoardEventHandler);
    }


    private void removeEventHandlers() {
        scene.removeEventHandler(KeyEvent.KEY_PRESSED, keyBoardEventHandler);
        for(Button b: digitalKeyBoard){
            b.removeEventHandler(MouseEvent.MOUSE_CLICKED, screenKeysEventHandler);
        }
    }


    private void updateRemainingStatement() {
        if(guessLeft >= 0)
            remainder.setText("Remaining Guesses: " + guessLeft);

        if(!won && guessLeft == 0)
            lost();
    }


    private boolean letterMatch(char c) {
        for(int i = 0; i < word.length(); i++) {
            if (c == word.charAt(i))
                return true;
        }
        return false;
    }


    private void updateDisplayedLetter(char c) {
        for(int i = 0; i < displayedLetters.size(); i++) {
            if (c == word.charAt(i)) {
                displayedLetters.get(i).setText(String.valueOf(c));
                lettersToGo--;
            }
        }
    }


    private void checkWin() {
        if(!won && lettersToGo == 0) {
            removeEventHandlers();
            won = true;
            lost = false;
            gameInProgress = false;
            saveBtn.setDisable(true);

            Stage popup = new Stage();
            popup.setTitle("Game Over");

            VBox box = new VBox();
            Text message = new Text("You Won");
            Button close = new Button("Close");
            close.setOnAction(event -> popup.close());
            box.getChildren().addAll(message,close);
            box.setAlignment(Pos.CENTER);
            box.setSpacing(15);

            Scene newScene = new Scene(box);
            popup.setScene(newScene);
            popup.setWidth(150);
            popup.setHeight(200);
            popup.show();
        }
    }


    private void lost() {
        lost = true;
        gameInProgress = false;
        saveBtn.setDisable(true);

        removeEventHandlers();
        updateRemainingLetters();

        Stage popup = new Stage();
        popup.setTitle("Game Over");

        VBox box = new VBox();
        Text message = new Text("You Lost (the word was " + word + ")");
        Button close = new Button("Close");
        close.setOnAction(event -> popup.close());
        box.getChildren().addAll(message,close);
        box.setAlignment(Pos.CENTER);
        box.setSpacing(15);

        Scene newScene = new Scene(box);
        popup.setScene(newScene);
        popup.setWidth(400);
        popup.setHeight(250);
        popup.show();

    }


    private void updateRemainingLetters() {
        System.out.println(word);
        for(int i = 0; i < word.length(); i++) {
            if(displayedLetters.get(i).getText().isEmpty()) {
                displayedLetters.get(i).setText(String.valueOf(word.charAt(i)));
                displayedLetters.get(i).setStyle("-fx-background-color: #ff8080;");
            }
        }
    }


    private void handleValidInput (char letterSelected){
        gameChanged = true;
        saveBtn.setDisable(false);

        int indexUpperCase = (int) letterSelected - 65; // A -> ascii = 65

        if(!digitalKeyBoard.get(indexUpperCase).isDisable()) {
            //System.out.println("Disabling Key : " + digitalKeyBoard.get(indexUpperCase).getText());
            digitalKeyBoard.get(indexUpperCase).setDisable(true);
            if(letterMatch(letterSelected)) {
                updateDisplayedLetter(letterSelected);
                if (!won)
                    checkWin();
            }

            else {
                guessLeft--;
                int i = guessLeft;
                updateRemainingStatement();
                updateHangman(i);
            }
        }
    }


    private void resetHangMan() {
        part1.setVisible(false);
        part2.setVisible(false);
        part3.setVisible(false);
        part4.setVisible(false);
        part5.setVisible(false);
        part6.setVisible(false);
        part7.setVisible(false);
        part8.setVisible(false);
        part9.setVisible(false);
        part10.setVisible(false);

        part1.setStrokeWidth(5);
        part2.setStrokeWidth(5);
        part3.setStrokeWidth(5);
        part4.setStrokeWidth(5);
        part6.setStrokeWidth(5);
        part7.setStrokeWidth(5);
        part8.setStrokeWidth(5);
        part9.setStrokeWidth(5);
        part10.setStrokeWidth(5);
    }

    private void resetGame() {
        gameChanged = false;

        won = false;
        lost = false;
        resetHangMan();
        word = "";
        lettersToGo = 0;
        guessLeft = 10;
        remainder.setText("Remaining Guesses: " + guessLeft);
        displayedLetters.clear();
        digitalKeyBoard.clear();
    }

    private void updateHangman(int i) {
        switch (i) {
            case 9:
                part1.setVisible(true);
                break;
            case 8:
                part2.setVisible(true);
                break;
            case 7:
                part3.setVisible(true);
                break;
            case 6:
                part4.setVisible(true);
                break;
            case 5:
                part5.setVisible(true);
                break;
            case 4:
                part6.setVisible(true);
                break;
            case 3:
                part7.setVisible(true);
                break;
            case 2:
                part8.setVisible(true);
                break;
            case 1:
                part9.setVisible(true);
                break;
            case 0:
                part10.setVisible(true);
                break;
            default:
                break;
        }
    }


    private void startNewGame(BorderPane whole, boolean loadingGame) {
        resetGame();
        resetHangMan();

        System.out.println("WORD RIGHT NOW: " + word);

        if (!loadingGame) {
            try {
                userPicksWord();
            } catch (Exception e) {
                startNewGame(whole,false);
            }
        }

        whole.setCenter(createGame());
    }



    private static void main(String[] args) {
        launch(args);
    }

}