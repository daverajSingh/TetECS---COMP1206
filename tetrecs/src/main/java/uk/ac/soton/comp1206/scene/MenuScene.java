package uk.ac.soton.comp1206.scene;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import javax.script.Bindings;

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
        imageView.setTranslateY(-200);
        menuPane.getChildren().add(imageView);

        this.multimedia.playBackgroundMusic("menu.mp3");

        //For now, let us just add a button that starts the game. I'm sure you'll do something way better.
        var singlePlayer = new Button("Single Player");
        var multiPlayer = new Button("Multi Player");
        var instructions = new Button("How to Play");
        var exit = new Button("Exit");


        singlePlayer.setBackground(null);
        multiPlayer.setBackground(null);
        instructions.setBackground(null);
        exit.setBackground(null);


        var vbox = new VBox(20, singlePlayer, multiPlayer, instructions, exit);
        menuPane.getChildren().add(vbox);

        vbox.getStyleClass().add("menuItem");
        vbox.setAlignment(Pos.BOTTOM_CENTER);

        //Bind the button action to the startGame method in the menu
        singlePlayer.setOnAction(this::startGame);

    }

    /**
     * Initialise the menu
     */
    @Override
    public void initialise() {

    }

    /**
     * Handle when the Start Game button is pressed
     * @param event event
     */
    private void startGame(ActionEvent event) {
        gameWindow.startChallenge();
        this.multimedia.stopBackground();
    }

}
