package uk.ac.soton.comp1206.scene;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.media.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The main menu of the game. Provides a gateway to the rest of the game.
 */
public class MenuScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);

    private Multimedia multimedia = new Multimedia();

    /**
     * Create a new menu scene
     * @param gameWindow the Game Window this will be displayed in
     */
    public MenuScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Menu Scene");
    }

    /**
     * Build the menu layout
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var menuPane = new StackPane();
        menuPane.setMaxWidth(gameWindow.getWidth());
        menuPane.setMaxHeight(gameWindow.getHeight());
        menuPane.getStyleClass().add("menu-background");
        root.getChildren().add(menuPane);

        var mainPane = new BorderPane();
        menuPane.getChildren().add(mainPane);

        //Better title
        Image titleImage = new Image(MenuScene.class.getResource("/images/TetrECS.png").toExternalForm());
        ImageView imageView = new ImageView(titleImage);
        imageView.setPreserveRatio(true);
        imageView.setFitHeight(150);
        imageView.setTranslateY(-150);
        menuPane.getChildren().add(imageView);

        //Animate Title
        RotateTransition rotateTransition = new RotateTransition(Duration.millis(5000), imageView);
        rotateTransition.setFromAngle(-10);
        rotateTransition.setToAngle(10);
        rotateTransition.setByAngle(0);
        rotateTransition.setCycleCount(Animation.INDEFINITE);
        rotateTransition.setInterpolator(Interpolator.EASE_BOTH);
        rotateTransition.setAutoReverse(true);
        rotateTransition.play();

        //Plays Background Music
        this.multimedia.playBackgroundMusic("menu.mp3");

        //Menu Buttons
        var singlePlayer = new Button("Single Player");
        var multiPlayer = new Button("Multi Player");
        var instructions = new Button("How to Play");
        var exit = new Button("Exit");

        //Vbox to store and display all buttons
        var vbox = new VBox(10, singlePlayer, multiPlayer, instructions, exit);
        menuPane.getChildren().add(vbox);

        //Styles buttons
        vbox.getStyleClass().add("menuItem");
        vbox.setAlignment(Pos.BOTTOM_CENTER);

        singlePlayer.setBackground(null);
        multiPlayer.setBackground(null);
        instructions.setBackground(null);
        exit.setBackground(null);

        //Button Actions
        singlePlayer.setOnAction(this::startGame);
        multiPlayer.setOnAction(this::startMultiplayer);
        instructions.setOnAction(this::startInstructions);
        exit.setOnAction((ActionEvent event) -> {
            Platform.exit();
        });

        //Editing what happens when hover
        for (Node node: vbox.getChildren()) {
            node.hoverProperty().addListener((ov, oldValue, newValue) -> {
                if (newValue) {
                    node.setStyle("-fx-text-fill: yellow");
                } else {
                    node.setStyle("-fx-text-fill: white");
                }
            });
            node.setStyle("-fx-text-fill: white");
        }


    }

    /**
     * Initialise the menu
     */
    @Override
    public void initialise() {
        //Escape Key Event
        scene.setOnKeyPressed(keyEvent -> {
            if(keyEvent.getCode() == KeyCode.ESCAPE) {
                logger.info("Escape Pressed");
                System.exit(0);
            }
        });
    }

    /**
     * Handle when the Start Game button is pressed
     * @param event event
     */
    private void startGame(ActionEvent event) {
        gameWindow.startChallenge();
        this.multimedia.stopBackground();
    }

    private void startMultiplayer(ActionEvent event) {

    }

    private void startInstructions(ActionEvent event) {
        gameWindow.startInstructions();
        multimedia.stopBackground();
    }
}
