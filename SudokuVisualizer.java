import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Random;

public class SudokuVisualizer extends JFrame {
    static final int SIZE = 9;
    private JTextField[][] cells;
    private SudokuSolver solver;
    private JPanel gridPanel;
    private boolean isSolved = false;

    public SudokuVisualizer() {
        solver = new SudokuSolver(this);
        cells = new JTextField[SIZE][SIZE];
        setTitle("Sudoku Solver");
        setSize(600, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.BLACK);  // Set background color

        gridPanel = new JPanel(new GridLayout(SIZE, SIZE, 0, 0)); // No gaps between cells
        gridPanel.setBackground(Color.BLACK);  // Set background color for grid panel
        gridPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));  // Add padding around the grid panel

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                cells[row][col] = new JTextField();
                cells[row][col].setHorizontalAlignment(JTextField.CENTER);
                cells[row][col].setBackground(Color.BLACK);  // Set cell background color
                cells[row][col].setForeground(Color.WHITE);  // Set text color to white
                cells[row][col].setFont(new Font("Arial", Font.BOLD, 30));  // Set font

                // Set custom borders for the 3x3 sub-grids
                Border border = BorderFactory.createMatteBorder(
                        (row % 3 == 0) ? 3 : 1,
                        (col % 3 == 0) ? 3 : 1,
                        (row % 3 == 2) ? 3 : 1,
                        (col % 3 == 2) ? 3 : 1,
                        Color.WHITE);
                cells[row][col].setBorder(border);

                // Add KeyListener to change text color on user input
                cells[row][col].addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyTyped(KeyEvent e) {
                        JTextField source = (JTextField) e.getSource();
                        char keyChar = e.getKeyChar();
                        if (Character.isDigit(keyChar) && keyChar != '0') {
                            source.setForeground(Color.YELLOW);  // Change text color to pink on user input
                        }
                    }

                    @Override
                    public void keyReleased(KeyEvent e) {
                        JTextField source = (JTextField) e.getSource();
                        String text = source.getText();
                        if (!text.isEmpty() && text.matches("[1-9]")) {
                            int row = getRow(source);
                            int col = getCol(source);
                            int value = Integer.parseInt(text);
                            int[][] board = readBoard();
                            if (solver.isSafe(board, row, col, value)) {
                                source.setForeground(Color.GREEN);  // Change text color to green if correct
                            } else {
                                source.setForeground(Color.YELLOW);  // Change text color to red if incorrect
                            }
                        } else {
                            source.setForeground(Color.YELLOW);  // Default color if input is not valid
                        }
                    }
                });

                gridPanel.add(cells[row][col]);
            }
        }
        add(gridPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.BLACK);  // Set background color for button panel

        JButton solveButton = new JButton("Solve");
        solveButton.setBackground(Color.CYAN);  // Set button background color
        solveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[][] board = readBoard();
                new SolverWorker(board).execute();
            }
        });
        buttonPanel.add(solveButton);

        JButton randomButton = new JButton("Random Board");
        randomButton.setBackground(Color.CYAN);  // Set button background color
        randomButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateRandomBoard();
            }
        });
        buttonPanel.add(randomButton);

        JButton clearButton = new JButton("Clear");
        clearButton.setBackground(Color.CYAN);  // Set button background color
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearBoard();
            }
        });
        buttonPanel.add(clearButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private int getRow(JTextField field) {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (cells[row][col] == field) {
                    return row;
                }
            }
        }
        return -1;  // Not found (should not happen)
    }

    private int getCol(JTextField field) {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (cells[row][col] == field) {
                    return col;
                }
            }
        }
        return -1;  // Not found (should not happen)
    }

    private int[][] readBoard() {
        int[][] board = new int[SIZE][SIZE];
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                String text = cells[row][col].getText();
                if (!text.isEmpty()) {
                    board[row][col] = Integer.parseInt(text);
                }
            }
        }
        return board;
    }

    public void updateBoard(int[][] board) {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                String currentText = cells[row][col].getText();
                String newText = board[row][col] == 0 ? "" : String.valueOf(board[row][col]);
                if (!currentText.equals(newText)) {
                    if (newText.isEmpty()) {
                        cells[row][col].setBackground(Color.BLACK);
                    } else if (solver.isSafe(board, row, col, board[row][col])) {
                        cells[row][col].setBackground(Color.GREEN);  // Correct number turns green
                    } else {
                        blinkCell(cells[row][col]);  // Blink red for incorrect attempts
                    }
                    cells[row][col].setText(newText);
                }
            }
        }
        // Set entire grid background to green upon solving
        if (!isSolved && solver.isSudokuSolved(board)) {
            gridPanel.setBackground(Color.GREEN);
            isSolved = true;
        }
    }

    private void blinkCell(JTextField cell) {
        final Color originalColor = cell.getBackground();
        cell.setBackground(Color.RED);
        Timer timer = new Timer(500, new ActionListener() {
            boolean toggled = false;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (toggled) {
                    cell.setBackground(Color.RED);
                } else {
                    cell.setBackground(originalColor);
                }
                toggled = !toggled;
            }
        });
        timer.setRepeats(false);  // Blink only once
        timer.start();
    }

    private void generateRandomBoard() {
        Random random = new Random();
        clearBoard(); // Clear the board before generating a new random board
        for (int i = 0; i < 9; i++) {
            int num = random.nextInt(SIZE) + 1; // Random number between 1 to SIZE
            int row = random.nextInt(SIZE);
            int col = random.nextInt(SIZE);
            cells[row][col].setText(String.valueOf(num));
        }
    }

    private void clearBoard() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                cells[row][col].setText("");
                cells[row][col].setBackground(Color.BLACK);  // Reset background color
                cells[row][col].setForeground(Color.WHITE);  // Reset text color to white
            }
        }
        gridPanel.setBackground(Color.BLACK); // Reset grid panel background color
        isSolved = false; // Reset solved status
    }

    private class SolverWorker extends SwingWorker<Boolean, Void> {
        private int[][] board;

        public SolverWorker(int[][] board) {
            this.board = board;
        }

        @Override
        protected Boolean doInBackground() throws Exception {
            return solver.solveSudoku(board);
        }

        @Override
        protected void done() {
            try {
                if (get()) {
                    JOptionPane.showMessageDialog(null, "Sudoku solved successfully!");
                } else {
                    JOptionPane.showMessageDialog(null, "No solution exists!");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}