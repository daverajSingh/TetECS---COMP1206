package uk.ac.soton.comp1206.scene;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
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
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.io.*;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Scanner;

public class ScoresScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(ScoresScene.class);

    private Multimedia multimedia = new Multimedia();

    protected Game gameState;

    protected int score;

    protected String name;

    protected SimpleListProperty<Pair<String, Integer>> localScoreList = new SimpleListProperty<>();

    /**
     * Create a new scene, passing in the GameWindow the scene will be displayed in
     *
     * @param gameWindow the game window
     */
    public ScoresScene(GameWindow gameWindow, Game game) {
        super(gameWindow);
        gameState = game;
        score = game.scoreProperty().get();
        this.localScoreList.set(FXCollections.observableArrayList(new ArrayList<Pair<String, Integer>>()));
        logger.info("Creating Scores Scene");
    }

    @Override
    public void initialise() {
        multimedia.playSound("explode.wav");
        multimedia.playBackgroundMusic("end.wav");
        loadScores();
        addScore(this.name, this.score);
        this.scene.setOnKeyPressed(keyEvent -> {
            if(keyEvent.getCode() == KeyCode.ESCAPE) {
                gameWindow.startMenu();
                logger.info("Escape Pressed");
            }
        });
    }

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

        var scores = new VBox();
        scores.setAlignment(Pos.CENTER);
        scorePane.getChildren().add(scores);

        Text highScores = new Text("Game Over - High Scores");
        mainPane.setTop(highScores);
        highScores.setTextAlignment(TextAlignment.CENTER);
        highScores.setLayoutX((gameWindow.getWidth() - highScores.layoutBoundsProperty().get().getWidth())/2);
        highScores.getStyleClass().add("title");

        var scoresList = new ScoresList();
        scores.getChildren().add(scoresList);
        scoresList.setAlignment(Pos.CENTER);
        this.localScoreList.bind(scoresList.listProperty());

        var nameDialog = new TextInputDialog();
        nameDialog.setTitle("Score Input");
        nameDialog.setContentText("Enter Name To Add to Leaderboard");
        Optional<String> result = nameDialog.showAndWait();
        this.name = "Anon";
        if(result.isPresent()){
            this.name = result.get();
        }
    }

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

    private void writeScores() {
        ArrayList<Pair<String, Integer>> scores = new ArrayList<>();
        File file = new File("scores.txt");
        try {
            file.createNewFile();

            scores.add(new Pair<>("Daveraj", 500));
            scores.add(new Pair<>("Daveraj", 400));
            scores.add(new Pair<>("Daveraj", 300));
            scores.add(new Pair<>("Daveraj", 200));
            scores.add(new Pair<>("Daveraj", 100));
            scores.add(new Pair<>("Daveraj", 50));

            FileWriter fileWriter = new FileWriter(file);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            for (Pair pair : scores) {
                String nameScore = pair.getKey() + ":" + pair.getValue();
                localScoreList.add(pair);
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
}
