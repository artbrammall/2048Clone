package TwentyFortyEight;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PImage;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * App.java – Main game logic and rendering for a 2048 clone.
 * Handles setup, drawing loop, user input, and overall board state.
 */

/*  - Understand code
    - Add comments explaining code
    - Let new spawned tile spawn slower visually
    - Upload build.gradle and src to ED, press Mark
 */
public class App extends PApplet {

    // GAME SETTINGS
    public static final int CELL_SIZE = 100;
    public static final int FPS = 60; // Frame-rate, times game redrawn per second
    public static int gridSize = 4; // Boards dimensions with default 4 (e.g. 4x4)
    public static int gridWidth; //
    public static int gridHeight;

    public static Random random = new Random();
    private boolean gameOver = false;
    public final Cell[][] BOARD = new Cell[gridSize][gridSize];

    // IMAGES
    public final HashMap<Integer, PImage> TILE_IMAGES = new HashMap<>(); // Stores all tile resources by value
    public PImage hoverImage; // Image shown when hovering over a cell
    public PImage notHoverImage;
    private PImage gameOverImage;

    // TIMER
    private int totalSeconds = 0; //
    private int previousSecond = 0;

    public App() {
    }

    @Override
    public void settings() {
        // Configures the size of the window
        gridWidth = gridSize * CELL_SIZE;
        gridHeight = gridSize * CELL_SIZE;
        size(gridWidth, gridHeight);
    }

    /**
     * Called once on startup. Sets up board, font, images, and starting tiles.
     */
    @Override
    public void setup() {
        frameRate(FPS);

        // Custom font for timer
        String fontPath = getClass().getResource("/TwentyFortyEight/data/Kalam-Bold.ttf").getPath().replace("%20", " ");
        PFont customFont = createFont(fontPath, 24);
        textFont(customFont);

        // Tile images
        int[] values = {2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192};
        for (int value : values) {
            String path = getClass().getResource("/TwentyFortyEight/" + value + ".png").getPath().replace("%20", " ");
            TILE_IMAGES.put(value, loadImage(path));
        }

        // Other images
        String gameOverPath = getClass().getResource("/TwentyFortyEight/gameover.png").getPath().replace("%20", " ");
        String hoverPath = getClass().getResource("/TwentyFortyEight/hover.png").getPath().replace("%20", " ");
        String notHoverPath = getClass().getResource("/TwentyFortyEight/nothover.png").getPath().replace("%20", " ");

        hoverImage = loadImage(hoverPath);
        notHoverImage = loadImage(notHoverPath);
        gameOverImage = loadImage(gameOverPath);

        // Initialise the board
        for (int i = 0; i < BOARD.length; i++) {
            for (int j = 0; j < BOARD[i].length; j++) {
                BOARD[i][j] = new Cell(j, i);
            }
        }

        // Two random tiles created to start
        createRandomTile();
        createRandomTile();
    }

    /**
     * Main loop of the game, ran every frame. Handles timer, drawing cells and background, and game over.
     */
    @Override
    public void draw() {
        background(158, 137, 116);

        // Update seconds past of current game
        int currentSecond = millis() / 1000;
        if (currentSecond > previousSecond) {
            totalSeconds++;
            previousSecond = currentSecond;
        }

        // Update animations and draw backgrounds
        for (Cell[] row : BOARD) {
            for (Cell cell : row) {
                cell.tick();
                cell.drawBackground(this);
            }
        }

        // Draw tiles with values
        for (Cell[] row : BOARD) {
            for (Cell cell : row) {
                cell.drawTile(this);
            }
        }

        // Draw timer in the top-right
        fill(255);
        textAlign(RIGHT, TOP);
        textSize(30);
        text("" + totalSeconds, gridWidth - 10, 0);

        // If the game is over, show image and end the game loop
        if (gameOver) {
            image(gameOverImage, 0, 0, gridWidth, gridHeight);
            noLoop();
        }
    }

    /**
     * Handles all key-presses, arrow keys and reset.
     */
    @Override
    public void keyPressed(KeyEvent keyEvent) {
        // Press 'R' to reset the game at any time
        if (keyEvent.getKey() == 'r' || keyEvent.getKey() == 'R') {
            resetGame();
            return;
        }

        int keyCode = keyEvent.getKeyCode();
        boolean moved = false;

        // Reset merge flag for all cells before a move
        for (Cell[] row : BOARD) {
            for (Cell cell : row) {
                cell.resetMerge();
            }
        }

        // If key is LEFT arrow
        if (keyCode == 37) {
            for (int targetRow = 0; targetRow < gridSize; targetRow++) {
                for (int column = 1; column < gridSize; column++) {
                    Cell currentCell = BOARD[targetRow][column];
                    // Find the farthest empty space
                    if (currentCell.hasValue()) {
                        int targetColumn = column;
                        while (targetColumn > 0 && BOARD[targetRow][targetColumn - 1].isEmpty()) {
                            targetColumn--;
                        }
                        // Try to merge with a cell of the same value
                        if (targetColumn > 0) {
                            Cell leftCell = BOARD[targetRow][targetColumn - 1];
                            if (leftCell.tryMergeWith(currentCell)) {
                                moved = true;
                                continue;
                            }
                        }
                        // Move to the farthest empty space
                        if (targetColumn != column) {
                            Cell destination = BOARD[targetRow][targetColumn];
                            destination.setDrawPosition(currentCell);
                            destination.setValue(currentCell.getValue());
                            destination.setTarget(targetColumn, targetRow);
                            currentCell.setValue(0);
                            moved = true;
                        }
                    }
                }
            }
        // If key is RIGHT arrow
        } else if (keyCode == 39) {
            for (int targetRow = 0; targetRow < gridSize; targetRow++) {
                for (int column = gridSize - 2; column >= 0; column--) {
                    Cell currentCell = BOARD[targetRow][column];
                    // Find the farthest empty space
                    if (currentCell.hasValue()) {
                        int targetColumn = column;
                        while (targetColumn < gridSize - 1 && BOARD[targetRow][targetColumn + 1].isEmpty()) {
                            targetColumn++;
                        }
                        // Try to merge with a cell of the same value
                        if (targetColumn < gridSize - 1) {
                            Cell rightCell = BOARD[targetRow][targetColumn + 1];
                            if (rightCell.tryMergeWith(currentCell)) {
                                moved = true;
                                continue;
                            }
                        }
                        // Move to the farthest empty space
                        if (targetColumn != column) {
                            Cell destination = BOARD[targetRow][targetColumn];
                            destination.setDrawPosition(currentCell);
                            destination.setValue(currentCell.getValue());
                            destination.setTarget(targetColumn, targetRow);
                            currentCell.setValue(0);
                            moved = true;
                        }
                    }
                }
            }
        // If key is UP arrow
        } else if (keyCode == 38) {
            for (int row = 1; row < gridSize; row++) {
                for (int targetColumn = 0; targetColumn < gridSize; targetColumn++) {
                    Cell currentCell = BOARD[row][targetColumn];
                    // Find the farthest empty space
                    if (currentCell.hasValue()) {
                        int targetRow = row;
                        while (targetRow > 0 && BOARD[targetRow - 1][targetColumn].isEmpty()) {
                            targetRow--;
                        }
                        // Try to merge with a cell of the same value
                        if (targetRow > 0) {
                            Cell aboveCell = BOARD[targetRow - 1][targetColumn];
                            if (aboveCell.tryMergeWith(currentCell)) {
                                moved = true;
                                continue;
                            }
                        }
                        // Move to the farthest empty space
                        if (targetRow != row) {
                            Cell destination = BOARD[targetRow][targetColumn];
                            destination.setDrawPosition(currentCell);
                            destination.setValue(currentCell.getValue());
                            destination.setTarget(targetColumn, targetRow);
                            currentCell.setValue(0);
                            moved = true;
                        }
                    }
                }
            }
        // If key is DOWN arrow
        } else if (keyCode == 40) {
            for (int row = gridSize - 2; row >= 0; row--) {
                for (int targetColumn = 0; targetColumn < gridSize; targetColumn++) {
                    Cell currentCell = BOARD[row][targetColumn];
                    // Find the farthest empty space
                    if (currentCell.hasValue()) {
                        int targetRow = row;
                        while (targetRow < gridSize - 1 && BOARD[targetRow + 1][targetColumn].isEmpty()) {
                            targetRow++;
                        }
                        // Try to merge with a cell of the same value
                        if (targetRow < gridSize - 1) {
                            Cell belowCell = BOARD[targetRow + 1][targetColumn];
                            if (belowCell.tryMergeWith(currentCell)) {
                                moved = true;
                                continue;
                            }
                        }
                        // Move to the farthest empty space
                        if (targetRow != row) {
                            Cell destination = BOARD[targetRow][targetColumn];
                            destination.setDrawPosition(currentCell);
                            destination.setValue(currentCell.getValue());
                            destination.setTarget(targetColumn, targetRow);
                            currentCell.setValue(0);
                            moved = true;
                        }
                    }
                }
            }
        }

        // If any tile was moved, create a new random tile and check for game over
        if (moved) {
            createRandomTile();
            if (isGameOver()) {
                gameOver = true;
            }
        }
    }

    /**
     * Extra feature to spawn a random tile when a cell is clicked.
     */
    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == PConstants.LEFT) {
            Cell currentPlace = BOARD[mouseEvent.getY() / App.CELL_SIZE][mouseEvent.getX() / App.CELL_SIZE];
            currentPlace.place();
        }
    }

    /**
     * Entry point to the game, with an optional custom grid size.
     */
    public static void main(String[] args) {
        if (args.length > 0) {
            try {
                int inputSize = Integer.parseInt(args[0]);
                if (inputSize > 1) {
                    gridSize = inputSize;
                }
            } catch (NumberFormatException ignored) {}
        }
        PApplet.main("TwentyFortyEight.App");
    }

    /**
     * Chooses a random cell that is empty, and creates a new 2 or 4 tile with a delay before appearance.
     */
    private void createRandomTile() {
        ArrayList<Cell> emptyCells = new ArrayList<>();
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if (BOARD[i][j].isEmpty()) {
                    emptyCells.add(BOARD[i][j]);
                }
            }
        }
        if (!emptyCells.isEmpty()) {
            Cell chosen = emptyCells.get(random.nextInt(emptyCells.size()));
            int newValue = (random.nextInt(2) + 1) * 2;
            chosen.setValue(newValue);
            chosen.delayCreate(150); // 0.25 seconds
        }
    }

    /**
     * Resets timer, clears the board, and creates two new random starting tiles.
     */
    private void resetGame() {
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                BOARD[i][j].clear();
                BOARD[i][j].resetMerge();
            }
        }
        totalSeconds = 0;
        previousSecond = millis() / 1000;
        gameOver = false;

        createRandomTile();
        createRandomTile();

        loop();
    }

    /**
     * Checks every cell. If there are no empty cells and no merges are possible, then the game is over.
     */
    private boolean isGameOver() {
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if (BOARD[i][j].isEmpty()) {
                    return false;
                }
                int value = BOARD[i][j].getValue();
                if (j + 1 < gridSize && BOARD[i][j + 1].getValue() == value) {
                    return false;
                }
                if (i + 1 < gridSize && BOARD[i + 1][j].getValue() == value) {
                    return false;
                }
            }
        }
        return true;
    }
}
