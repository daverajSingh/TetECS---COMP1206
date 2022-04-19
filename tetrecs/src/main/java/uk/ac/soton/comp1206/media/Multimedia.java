package uk.ac.soton.comp1206.media;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Multimedia {
    private static final Logger logger = LogManager.getLogger(Multimedia.class);

    private static MediaPlayer mediaPlayer;
    private static MediaPlayer backgroundPlayer;

    public void playBackgroundMusic(String music) {
        String musicToBePlayed = Multimedia.class.getResource("/music/" + music).toExternalForm();
        try {
            Media play = new Media(musicToBePlayed);
            backgroundPlayer = new MediaPlayer(play);

            backgroundPlayer.setAutoPlay(true);
            backgroundPlayer.setCycleCount(MediaPlayer.INDEFINITE);

            Duration end = play.getDuration();
            backgroundPlayer.setStartTime(Duration.seconds(0));
            backgroundPlayer.setStopTime(end);

            backgroundPlayer.play();
            logger.info("Playing Background Music: " + music);
        } catch(Exception e) {
            e.printStackTrace();
            logger.error(e.toString());
        }
    }

    public void playSound(String sound) {
        String soundToPlay = Multimedia.class.getResource("/sounds/" + sound).toExternalForm();
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

    public void stopBackground() {
        backgroundPlayer.stop();
        logger.info("Background Music Stopped");
    }

}
