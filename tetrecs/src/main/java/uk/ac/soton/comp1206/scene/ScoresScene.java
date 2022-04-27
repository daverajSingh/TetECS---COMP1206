package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.ScoresList;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.media.Multimedia;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.io.*;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Scanner;

/**
 * The ScoresScene will hold and display a list of names and associated scores. It will load an ordered list of scores
 * and prompt the user for their name to add to the leaderboard.
 * The ScoreScene also shows online scores
 */
public class ScoresScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(ScoresScene.class);

    private Multimedia multimedia = new Multimedia();

    /**
     * The last game
     */
    protected Game gameState;

    /**
     * The score that the player achieved
     */
    protected int score;

    /**
     * The name of the player
     */
    protected String name;

    /**
     * The local scores that the player has achieved
     */
    protected SimpleListProperty<Pair<String, Integer>> localScoreList = new SimpleListProperty<>();

    /**
     * Online scores that are retrieved from the server
     */
    protected SimpleListProperty<Pair<String, Integer>> remoteScoresList = new SimpleListProperty<>();

    /**
     * Communicator is used to communicate with the server
     */
    protected Communicator communicator;

    protected boolean isMultiplayer = false;
    /**
     * Create a new scene, passing in the GameWindow the scene will be displayed in
     *
     * @param gameWindow the game window
     */

    protected SimpleListProperty<Pair<String, Integer>> multiplayerScores;

    public ScoresScene(GameWindow gameWindow, Game game) {
        super(gameWindow);
        gameState = game;
        score = game.scoreProperty().get(); // sets the score that the player achieved
        this.localScoreList.set(FXCollections.observableArrayList(new ArrayList<Pair<String, Integer>>()));
        this.remoteScoresList.set(FXCollections.observableArrayList(new ArrayList<Pair<String, Integer>>()));
        logger.info("Creating Scores Scene");
        communicator = gameWindow.getCommunicator();
    }

    /**
     * Create a new scene, passing in the GameWindow the scene will be displayed in, and passing in multiplayer scores
     * which will displayed as a scorelist
     * @param gameWindow the game window
     */
    public ScoresScene(GameWindow gameWindow, Game game, SimpleListProperty<Pair<String, Integer>> scores) {
        super(gameWindow);
        gameState = game;
        score = game.scoreProperty().get(); //sets the score that the player achieved
        //Sets the localscorelist to the scores in multiplayer
        this.localScoreList.set(FXCollections.observableArrayList(new ArrayList<Pair<String, Integer>>()));
        this.remoteScoresList.set(FXCollections.observableArrayList(new ArrayList<Pair<String, Integer>>()));
        multiplayerScores = scores;
        logger.info("Creating Scores Scene");
        communicator = gameWindow.getCommunicator();
        isMultiplayer = true;
    }

    /**
     * Initialise this scene. Called after creation
     */
    @Override
    public void initialise() {
        multimedia.playSound("explode.wav");
        multimedia.playBackgroundMusic("end.wav");
        if(!isMultiplayer) {
            loadScores();
            addScore(this.name, this.score);
        } else {
            this.localScoreList.addAll(multiplayerScores);
            this.localScoreList.sort((a, b) -> b.getValue() - a.getValue());
        }
        this.scene.setOnKeyPressed(keyEvent -> {
            if(keyEvent.getCode() == KeyCode.ESCAPE) {
                multimedia.playSound("transition.wav");
                gameWindow.startMenu();
                logger.info("Escape Pressed");
                if(isMultiplayer) {
                    communicator.send("PART");
                }
            }
        });
        loadOnlineScores();
        communicator.addListener(message -> Platform.runLater(() -> receiveCommunication(message.trim())));
    }

    /**
     * Build the layout of the scene
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

        var scorePane = new StackPane();
        scorePane.setMaxWidth(gameWindow.getWidth());
        scorePane.setMaxHeight(gameWindow.getHeight());
        scorePane.getStyleClass().add("menu-background");
        root.getChildren().add(scorePane);

        var mainPane = new BorderPane();
        scorePane.getChildren().add(mainPane);

        //Contains boths scorelists
        var scores = new HBox();
        scores.setAlignment(Pos.CENTER);
        scorePane.getChildren().add(scores);

        //Local leaderboard
        var localScores = new VBox();
        localScores.setAlignment(Pos.CENTER);
        scores.getChildren().add(localScores);

        //Public leaderboard on the server
        var onlineScores = new VBox();
        onlineScores.setAlignment(Pos.CENTER);
        scores.getChildren().add(onlineScores);

        //Game over title text
        Text highScores = new Text("Game Over - High Scores");
        highScores.setTextAlignment(TextAlignment.CENTER);
        highScores.getStyleClass().add("title");
        scorePane.getChildren().add(highScores);
        StackPane.setAlignment(highScores, Pos.TOP_CENTER);

        //Local leaderboard text
        var scoreText = new Text("Local Scores");
        scoreText.getStyleClass().add("heading");
        localScores.getChildren().add(scoreText);
        scoreText.setTranslateX(300);

        //local leaderboard scorelist
        var scoresList = new ScoresList();
        localScores.getChildren().add(scoresList);
        scoresList.setAlignment(Pos.CENTER);
        scoresList.setTranslateX(300);
        this.localScoreList.bind(scoresList.listProperty());

        //Online leaderboard text
        var onlineScoreText = new Text("Online Scores");
        onlineScoreText.getStyleClass().add("heading");
        onlineScores.getChildren().add(onlineScoreText);
        onlineScoreText.setTranslateX(-300);

        //Online leaderboard scorelist
        var onlineScoresList = new ScoresList();
        onlineScores.getChildren().add(onlineScoresList);
        onlineScoresList.setAlignment(Pos.CENTER);
        onlineScoresList.setTranslateX(-300);
        this.remoteScoresList.bind(onlineScoresList.listProperty());

        //Asks the user for their name
        if(!isMultiplayer) {
            var nameDialog = new TextInputDialog();
            nameDialog.setTitle("Score Input");
            nameDialog.setContentText("Enter Name To Add to Leaderboard");
            Optional<String> result = nameDialog.showAndWait();
            this.name = result.orElse("Anon");
        }

        //Exit to main menu button
        var exit = new Button("Exit To Main Menu");
        exit.setBackground(null);
        scorePane.getChildren().add(exit);
        exit.setOnAction(this::startMenu);
        exit.setAlignment(Pos.CENTER);
        exit.setTranslateY(200);
        exit.getStyleClass().add("menuItem");

        //Adds hover property to the button
        exit.hoverProperty().addListener((ov, oldValue, newValue) -> {
            if (newValue) {
                exit.setStyle("-fx-text-fill: yellow");
            } else {
                exit.setStyle("-fx-text-fill: white");
            }
        });
        exit.setStyle("-fx-text-fill: white");
    }

    /**
     * Loads scores from a local file if available, otherwise it will generate a new file of scores
     */
    protected void loadScores() {
        File file = new File("scores.txt");
        try {
            var fileCreate = file.createNewFile();
            if (fileCreate) {
                writeScores();
            } else {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                Scanner scanner = new Scanner(reader);
                while (scanner.hasNext()) {
                    String[] nameScore = scanner.next().split(":");
                    var entry = new Pair<String, Integer>(nameScore[0], Integer.parseInt(nameScore[1]));
                    this.localScoreList.add(entry);
                }
                scanner.close();
            }
        } catch (Exception e) {
            logger.error("Unable to complete file making");
            e.printStackTrace();
        }
    }

    /**
     * Generates a preset file of new scores
     */
    private void writeScores() {
        ArrayList<Pair<String, Integer>> scores = new ArrayList<>();
        File file = new File("scores.txt");
        try {
            file.createNewFile(); // creates a new file

            scores.add(new Pair<>("Daveraj", 1000));
            scores.add(new Pair<>("Daveraj", 900));
            scores.add(new Pair<>("Daveraj", 800));
            scores.add(new Pair<>("Daveraj", 700));
            scores.add(new Pair<>("Daveraj", 600));
            scores.add(new Pair<>("Daveraj", 500));

            FileWriter fileWriter = new FileWriter(file);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            for (Pair pair : scores) {
                String nameScore = pair.getKey() + ":" + pair.getValue();
                localScoreList.add(pair); // adds score and name to the localscorelist as a pair
                bufferedWriter.write(nameScore);
                bufferedWriter.write("\n");
            }
            bufferedWriter.close();
            fileWriter.close();


        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error writing to file");
        }
    }

    /**
     * Adds a given name and score to the local file, and sorts the localScoreList to be displayed
     * @param name
     * @param score
     */
    public void addScore(String name, int score) {
        File file = new File("scores.txt");
        try{
            FileWriter fileWriter = new FileWriter(file, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(name + ":" + score);
            bufferedWriter.write("\n");
            bufferedWriter.close();
            fileWriter.close();

            this.localScoreList.add(new Pair<String, Integer>(this.name, this.score));
            this.localScoreList.sort((a, b) -> b.getValue() - a.getValue());
        } catch (Exception e){
            e.printStackTrace();
            logger.error("Unable to add score to text file");
        }
    }

    /**
     * Sends message to receive highscores from the server
     */
    protected void loadOnlineScores() {
        communicator.send("HISCORES");
    }

    /**
     * Writes a new score to the online server
     */
    protected void writeOnlineScore() {
        communicator.send("HISCORE " + this.name + ":" + this.score);
    }

    /**
     * Handles when the communicator receives a message
     * @param message
     */
    protected void receiveCommunication(String message) {
        if(message.contains("NEWSCORE")) { //server has received highscore
            logger.info("Server received highscore");
        } else if (message.contains("HISCORES")) { //Otherwise, the message is going to be received highscores from the server
            message = message.replace("HISCORES", "");
            String[] pairs = message.split("\n");
            for (String pair : pairs) { //adds scores and name to remoteScoresList
                String[] scoreName = pair.split(":");
                remoteScoresList.add(new Pair<>(scoreName[0], Integer.parseInt(scoreName[1])));
            }
            if(remoteScoresList.get(8).getValue() < this.score) {
                writeOnlineScore();
                // If the score is greater than the lowest score on the online score list, a new score will be sent
            }
        }
    }

    /**
     * Starts the menu when the player leaves the scoresScene
     * @param event
     */
    protected void startMenu(ActionEvent event) {
        gameWindow.startMenu();
        multimedia.stopBackground();
        multimedia.playSound("transition.wav");
        if(isMultiplayer) {
            communicator.send("PART");
        }
    }
}
