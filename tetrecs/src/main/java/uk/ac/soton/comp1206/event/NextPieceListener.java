package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.GamePiece;

/**
 * The NextPieceListener listens to new pieces inside game and calls an appropriate method.
 * When a new piece is provided by the game, the UI cna display the current GamePiece and following GamePiece
 */
public interface NextPieceListener {
    /**
     * Used to update the Current GamePiece and Following GamePiece
     * @param nextGamePiece
     * @param followingGamePiece
     */
    void nextPiece(GamePiece nextGamePiece, GamePiece followingGamePiece);
}
