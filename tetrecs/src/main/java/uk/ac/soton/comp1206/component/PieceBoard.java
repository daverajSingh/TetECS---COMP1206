package uk.ac.soton.comp1206.component;

import uk.ac.soton.comp1206.game.GamePiece;

public class PieceBoard extends GameBoard{


    public PieceBoard(int cols, int rows, double width, double height) {
        super(cols, rows, width, height);
    }



    public void pieceToDisplay(GamePiece gamePiece) {
        this.grid.clearGrid();
        this.grid.playPiece(gamePiece, 1,1);

    }

}
