package uk.ac.soton.comp1206.media;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Multimedia Class is used to play any music or sound resource in every scene
 */
public class Multimedia {
    private static final Logger logger = LogManager.getLogger(Multimedia.class);

    /**
     * mediaPlayer is used to play sounds
     */
    private static MediaPlayer mediaPlayer;

    /**
     * backgroundPlayer is used to play and background music
     */
    private static MediaPlayer backgroundPlayer;

    /**
     * Uses the backgroundPlayer and plays the given music
     * @param music
     */
    public void playBackgroundMusic(String music) {
        String musicToBePlayed = Multimedia.class.getResource("/music/" + music).toExternalForm();
        //uses the given string to find the wanted music to be played in the resources folder
        try {
            Media play = new Media(musicToBePlayed);
            backgroundPlayer = new MediaPlayer(play);

            backgroundPlayer.setAutoPlay(true); //Cycles Music Indefinetely
            backgroundPlayer.setCycleCount(MediaPlayer.INDEFINITE);

            //Sets start and end of music to allow for loop
            Duration end = play.getDuration();
            backgroundPlayer.setStartTime(Duration.seconds(0));
            backgroundPlayer.setStopTime(end);

            //Plays music
            backgroundPlayer.play();
            logger.info("Playing Background Music: " + music);
        } catch(Exception e) {
            e.printStackTrace();
            logger.error(e.toString());
        }
    }

    /**
     * Uses the mediaPlayer and plays the given sound
     * @param sound
     */
    public void playSound(String sound) {
        String soundToPlay = Multimedia.class.getResource("/sounds/" + sound).toExternalForm();
        //uses the given string to find the wanted sound to be played in the resources folder

        try {
            Media play = new Media(soundToPlay);
            mediaPlayer = new MediaPlayer(play);

            mediaPlayer.play();
            logger.info("Playing Media Sound: " + sound);
        } catch(Exception e) {
            e.printStackTrace();
            logger.error(e.toString());
        }
    }

    /**
     * Stops playing background Music
     */
    public void stopBackground() {
        backgroundPlayer.stop();
        logger.info("Background Music Stopped");
    }

}
