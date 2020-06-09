package crossword;

import java.util.Scanner;
import java.util.Optional;
import java.io.Console;
import crossword.CrosswordModel.Square;

public class CrosswordViewCLI {

  private final Console console = System.console();

  private CrosswordModel model;

  private void printf(String fmt, Object... args) {
    console.printf(fmt, args);
  }

  private void println(String line) {
    printf("%s\n", line);
  }

  private String readLine(String fmt, Object... args) {
    return console.readLine(fmt, args);
  }

  private int readNat(String prompt) {
    while (true) {
      try {
        int n = Integer.parseInt(readLine(prompt));
        if (n <= 0) throw new IllegalArgumentException();
        return n;
      } catch(Exception e) {
        println("Enter an integer greater than 0");
      }
    }
  }

  private void initModel() {
    int rows = readNat("How many rows? "),
        cols = readNat("How many columns? ");
    model = new CrosswordModel(rows, cols);
  }

  private void printRowBoundary() {
    for (int c = 0; c < model.getWidth(); c++) {
      printf("+---");
    }
    println("+");
  }

  private char squareToChar(CrosswordModel.Square sq) {
    return switch (sq.getType()) {
      case EMPTY -> ' ';
      case BLOCK -> '\u2589';
      case LETTER -> sq.getLetter();
    };
  }

  private void printRow(int r) {
    for (int c = 0; c < model.getWidth(); c++) {
      printf("| %s ", squareToChar(model.getSquareAt(r, c)));
    }
    println("|");
  }

  private void printBoard() {
    for (int r = 0; r < model.getHeight(); r++) {
      printRowBoundary();
      printRow(r);
    }
    printRowBoundary();
  }

  private static enum Command {
    EXIT("exit"), HELP("help"), CLEAR("clear"), BLOCK("block"), WRITE("write"),
    UNRECOGNIZED("");

    public final String cmd;

    Command(String cmd) {
      this.cmd = cmd;
    }

    public static Command of(String cmd) {
      for (Command c : Command.values()) {
        if (c.cmd.equals(cmd))
          return c;
      }
      return UNRECOGNIZED;
    }
  }

  private Optional<Square> nextSquare(Scanner args) {
    try {
      return Optional.of(model.getSquareAt(args.nextInt(), args.nextInt()));
    } catch(Exception e) {
      println("Invalid square");
      return Optional.empty();
    }
  }

  private void doExit(Scanner args) {
    System.exit(0);
  }

  private void doHelp(Scanner args) {
    println("Available commands:");
    println("exit");
    println("help");
    println("clear row col");
    println("block row col");
    println("write letter row col");
  }

  private void doClear(Scanner args) {
    nextSquare(args).ifPresent(sq -> {
      sq.clear();
      printBoard();
    });
  }

  private void doBlock(Scanner args) {
    nextSquare(args).ifPresent(sq -> {
      sq.block();
      printBoard();
    });
  }

  private void doWrite(Scanner args) {
    if (!args.hasNext()) {
      println("Provide a letter");
      return;
    }
    String letterStr = args.next();
    if (letterStr.length() != 1) {
      println("Provide a single letter");
      return;
    }
    char letter = Character.toUpperCase(letterStr.charAt(0));
    nextSquare(args).ifPresent(sq -> {
      sq.write(letter);
      printBoard();
    });
  }

  private void doUnrecognized(Scanner args) {
    println("Unrecognized command");
  }

  private void doCommand() {
    var line = readLine("> ");
    var scan = new Scanner(line);
    if (!scan.hasNext()) return;
    Command cmd = Command.of(scan.next());
    switch (cmd) {
      case EXIT: doExit(scan); break;
      case HELP: doHelp(scan); break;
      case CLEAR: doClear(scan); break;
      case BLOCK: doBlock(scan); break;
      case WRITE: doWrite(scan); break;
      case UNRECOGNIZED: doUnrecognized(scan); break;
    }
  }

  public void run() {
    initModel();
    printBoard();
    while (true) doCommand();
  }
}
