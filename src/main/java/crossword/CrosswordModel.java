package crossword;

import java.util.Optional;
import java.util.function.UnaryOperator;

public class CrosswordModel {

  public static class Square {

    public static enum Type {
      EMPTY, BLOCK, LETTER;
    }

    private static final char EMPTY = '\0', BLOCK = ' ';

    private char letter = EMPTY;

    public void clear() {
      letter = EMPTY;
    }

    public void block() {
      letter = BLOCK;
    }

    public boolean isValidLetter(char c) {
      return Character.isAlphabetic(c);
    }

    public void write(char c) {
      if (!isValidLetter(c))
        throw new IllegalArgumentException("Invalid letter: " + c);
      letter = c;
    }

    public Type getType() {
      return switch (letter) {
        case EMPTY -> Type.EMPTY;
        case BLOCK -> Type.BLOCK;
        default -> Type.LETTER;
      };
    }

    public char getLetter() {
      if (!getType().equals(Type.LETTER))
        throw new IllegalStateException("Not a letter");
      return letter;
    }
  }

  private final int rows, cols;
  private final Square[][] squares;

  public CrosswordModel(int rows, int cols) {
    this.rows = rows;
    this.cols = cols;
    squares = new Square[rows][cols];
    forEachSquare((r, c, s) -> squares[r][c] = new Square());
  }

  public int getHeight() {
    return rows;
  }

  public int getWidth() {
    return cols;
  }

  public boolean squareExists(int row, int col) {
    return 0 <= row && row < rows && 0 <= col && col < cols;
  }

  public Square getSquareAt(int row, int col) {
    return squares[row][col];
  }

  @FunctionalInterface
  public static interface SquareConsumer {
    public void accept(int row, int col, Square square);
  }

  public void forEachSquare(SquareConsumer f) {
    for (int r = 0; r < getHeight(); r++) {
      for (int c = 0; c < getWidth(); c++) {
        f.accept(r, c, getSquareAt(r, c));
      }
    }
  }
}
