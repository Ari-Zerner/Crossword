package crossword;

import java.util.Optional;
import java.util.function.UnaryOperator;

public class CrosswordModel {

  public static class Square {

    public static enum Type {
      EMPTY, BLOCK, LETTER;
    }

    private static final char EMPTY = '\0', BLOCK = ' ';

    private final CrosswordModel parent;

    private char letter = EMPTY;
    private Optional<Integer> number;

    private Square(CrosswordModel parent) {
      this.parent = parent;
    }

    private void clearNumber() {
      number = Optional.empty();
    }

    private void setNumber(int num) {
      number = Optional.of(num);
    }

    private void setLetter(char newLetter) {
      boolean renumberNeeded = letter != newLetter &&
                               (letter == BLOCK || newLetter == BLOCK);
      letter = newLetter;
      if (renumberNeeded) parent.renumber();
    }

    public void clear() {
      setLetter(EMPTY);
    }

    public void block() {
      setLetter(BLOCK);
    }

    public boolean isValidLetter(char c) {
      return Character.isAlphabetic(c);
    }

    public void write(char c) {
      if (!isValidLetter(c))
        throw new IllegalArgumentException("Invalid letter: " + c);
      setLetter(c);
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

    public Optional<Integer> getNumber() {
      return number;
    }
  }

  private final int rows, cols;
  private final Square[][] squares;

  public CrosswordModel(int rows, int cols) {
    this.rows = rows;
    this.cols = cols;
    squares = new Square[rows][cols];
    forEachSquare((r, c, s) -> squares[r][c] = new Square(this));
    renumber();
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

  private boolean isBlockOrEdge(int row, int col) {
    return !squareExists(row, col) ||
           getSquareAt(row, col).getType() == Square.Type.BLOCK;
  }

  private boolean hasNumber(int row, int col) {
    return !isBlockOrEdge(row, col) &&
           ((isBlockOrEdge(row - 1, col) && !isBlockOrEdge(row + 1, col)) ||
            (isBlockOrEdge(row, col - 1) && !isBlockOrEdge(row, col + 1)));
  }

  private void renumber() {
    forEachSquare(new SquareConsumer() {
      int next = 1;

      public void accept(int row, int col, Square square) {
        if (hasNumber(row, col)) {
          square.setNumber(next);
          next++;
        } else {
          square.clearNumber();
        }
      }
    });
  }
}
