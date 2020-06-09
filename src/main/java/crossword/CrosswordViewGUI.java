package crossword;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import crossword.CrosswordModel.Square;
import static java.awt.event.KeyEvent.*;

public class CrosswordViewGUI {

  private static int POINTS_PER_INCH = 72;

  private int squareSize; // pixels
  private int fontSize; // points

  private CrosswordModel model;

  private JPanel panel;

  private JRadioButton noAdvance, horizontal, vertical;

  private Graphics g;

  private int selectedRow = 0, selectedCol = 0;

  private int showNatInputDialog(String prompt, Integer def) {
    while (true) {
      try {
        String input = JOptionPane.showInputDialog(null, prompt, def);
        if (input == null) System.exit(0);
        int n = Integer.parseInt(input);
        if (n <= 0) throw new IllegalArgumentException();
        return n;
      } catch(Exception e) {
        JOptionPane.showMessageDialog(null, "Enter an integer greater than 0.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private void initModel() {
    int rows = showNatInputDialog("How many rows?", 10);
    int cols = showNatInputDialog("How Many Columns?", rows);
    model = new CrosswordModel(rows, cols);
  }

  private void initSize() {
    int spi = showNatInputDialog("How many squares per inch?", 4);
    squareSize = Toolkit.getDefaultToolkit().getScreenResolution() / spi;
    fontSize = POINTS_PER_INCH / spi;
  }

  private void drawEmpty(boolean isSelected) {
    if (isSelected) {
      g.setColor(new Color(0xB4D5FE)); // https://stackoverflow.com/questions/16094837/what-is-the-browser-default-background-color-when-selecting-text
      g.fillRect(0, 0, squareSize, squareSize);
      g.setColor(Color.BLACK);
    }
    g.drawRect(0, 0, squareSize, squareSize);
  }

  private void drawBlock(boolean isSelected) {
    if (isSelected) g.setColor(new Color(0x022E64));
    g.fillRect(0, 0, squareSize, squareSize);
    if (isSelected) g.setColor(Color.BLACK);
  }

  private void drawLetter(char letter, boolean isSelected) {
    drawEmpty(isSelected);
    String text = "" + letter;
    var fm = g.getFontMetrics();
    int x = (squareSize - fm.stringWidth(text)) / 2,
        y = squareSize - (squareSize - fm.getAscent()) / 2;
    g.drawString(text, x, y);
  }

  private void drawSquare(Square sq, boolean isSelected) {
    switch (sq.getType()) {
      case EMPTY:
        drawEmpty(isSelected);
        break;
      case BLOCK:
        drawBlock(isSelected);
        break;
      case LETTER:
        drawLetter(sq.getLetter(), isSelected);
        break;
    }
  }

  private void drawCrossword(Graphics g) {
    g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, squareSize));
    model.forEachSquare((row, col, sq) -> {
      this.g = g.create(col * squareSize, row * squareSize, squareSize, squareSize);
      boolean isSelected = row == selectedRow && col == selectedCol;
      drawSquare(sq, isSelected);
    });
  }

  private void repaint() {
    panel.repaint();
  }

  private void selectSquare(MouseEvent e) {
    int row = e.getY() / squareSize;
    int col = e.getX() / squareSize;
    if (0 <= row && row <= model.getHeight() &&
        0 <= col && col <= model.getWidth()) {
      selectedRow = row;
      selectedCol = col;
    }
    repaint();
  }

  private void autoAdvance(boolean forward) {
    int delta = forward ? 1 : -1;
    if (horizontal.isSelected()) {
      int newCol = selectedCol + delta;
      if (model.squareExists(selectedRow, newCol) &&
          !model.getSquareAt(selectedRow, newCol).getType().equals(Square.Type.BLOCK)) {
        selectedCol = newCol;
      }
    } else if (vertical.isSelected()) {
      int newRow = selectedRow + delta;
      if (model.squareExists(newRow, selectedCol) &&
          !model.getSquareAt(newRow, selectedCol).getType().equals(Square.Type.BLOCK)) {
        selectedRow = newRow;
      }
    }
  }

  private void handleKey(KeyEvent e) {
    int code = e.getKeyCode();
    if (code == VK_RIGHT) {
      selectedCol = (selectedCol + 1) % model.getWidth();
    } else if (code == VK_DOWN) {
      selectedRow = (selectedRow + 1) % model.getHeight();
    } else if (code == VK_LEFT) {
      selectedCol = (selectedCol - 1 + model.getWidth()) % model.getWidth();
    } else if (code == VK_UP) {
      selectedRow = (selectedRow - 1 + model.getHeight()) % model.getHeight();
    } else if (code == VK_ENTER) {
      autoAdvance(true);
    } else {
      Square sq = model.getSquareAt(selectedRow, selectedCol);
      if (code == VK_BACK_SPACE) {
        sq.clear();
        autoAdvance(false);
      } else if (code == VK_SPACE) {
        sq.block();
        autoAdvance(true);
      } else if (VK_A <= code && code <= VK_Z) {
        sq.write((char) code); // VK_A-Z correspond to Unicode
        autoAdvance(true);
      } else {
        return;
      }
    }
    repaint();
  }

  private void addListeners(JPanel panel) {
    panel.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        selectSquare(e);
        panel.requestFocusInWindow();
      }
    });
    panel.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        handleKey(e);
      }
    });
  }

  private Component initCrosswordPanel() {
    panel = new JPanel() {
      public void paintComponent(Graphics g) {
        drawCrossword(g);
      }
    };
    panel.setPreferredSize(new Dimension(model.getWidth() * squareSize, model.getHeight() * squareSize));
    addListeners(panel);
    return panel;
  }

  private Component initAdvanceButtons() {
    noAdvance = new JRadioButton("None", true);
    horizontal = new JRadioButton("Horizontal");
    vertical = new JRadioButton("Vertical");
    var all = new JRadioButton[]{noAdvance, horizontal, vertical};
    ButtonGroup group = new ButtonGroup();
    JPanel buttonPanel = new JPanel();
    buttonPanel.add(new JLabel("Auto-advance direction:"));
    for (var b : all) {
      group.add(b);
      buttonPanel.add(b);
      b.setFocusable(false);
    }
    buttonPanel.setFocusable(false);
    return buttonPanel;
  }

  private void initFrame() {
    var frame = new JFrame("Crossword");
    frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
    frame.add(initCrosswordPanel());
    frame.add(initAdvanceButtons());
    frame.pack();
    frame.setResizable(false);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setVisible(true);
    panel.requestFocusInWindow();
  }

  public void run() {
    initModel();
    initSize();
    initFrame();
  }
}
