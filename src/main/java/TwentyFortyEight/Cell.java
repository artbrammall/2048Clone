package TwentyFortyEight;

import processing.core.PImage;

/**
 * Cell.java – Represents one tile on the board.
 * Stores value, position, animation state, and handles drawing/merging.
 */
public class Cell {

    // GRID COORDINATES
    private final int X;
    private final int Y;

    // CELL STATES
    private int value; // If 0, cell is empty - otherwise, will be powers of 2
    private boolean hasMerged = false; // Prevents double merging

    // DRAWING AND ANIMATION
    private static final float MARGIN = 5f; // Spacing between tiles
    private float drawX; // Pixel coordinates of drawn tile and animation
    private float drawY;
    private int targetX; // Target grid coordinates of animation
    private int targetY;

    private boolean moving = false; // True if the cell is currently being animated
    private int createDelay = 0; // Milliseconds remaining before a tile appears

    /**
     * Constructor for a cell at grid position (x, y).
     */
    public Cell(int x, int y) {
        this.X = x;
        this.Y = y;
        this.value = 0;

        this.drawX = x * App.CELL_SIZE;
        this.drawY = y * App.CELL_SIZE;
        this.targetX = x;
        this.targetY = y;
        this.createDelay = 0;
    }

    // VALUE GETTERS AND SETTERS

    public void setValue(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public boolean hasValue() {
        return this.value != 0;
    }

    public boolean isEmpty() {
        return this.value == 0;
    }

    public void clear() {
        this.value = 0;
    }

    public void markMerged() {
        hasMerged = true;
    }

    public void resetMerge() {
        hasMerged = false;
    }

    /**
     * Sets a tile's delay animation timer.
     */
    public void delayCreate(int milliseconds) {
        this.createDelay = milliseconds;
    }

    /**
     * Attempts to merge a cell with another.
     * Cells are only merged if their values are the same, resulting in a doubled value.
     */
    public boolean tryMergeWith(Cell other) {
        if (this.value == other.value && !this.hasMerged) {
            this.setDrawPosition(other); // Start animation from moving merged cell
            this.setValue(this.value * 2);
            this.setTarget(this.X, this.Y); // Animate towards the stationary merged cell
            this.markMerged();
            other.setValue(0); // Removes moving merged cell
            return true;
        }
        return false;
    }

    // ANIMATION

    /**
     * Tells a cell where to animate to
     */
    public void setTarget(int x, int y) {
        this.targetX = x;
        this.targetY = y;
        this.moving = true;
    }

    /**
     * Sets the position where an animation should begin
     */
    public void setDrawPosition(Cell other) {
        this.drawX = other.drawX;
        this.drawY = other.drawY;
    }

    /**
     * Increments drawX and drawY each frame until they are at the target animation position.
     * Results in smooth animation.
     */
    public void tick() {
        // Subtract a small amount of time from createDelay each frame
        if (createDelay > 0) {
            createDelay -= 1000 / App.FPS;
            if (createDelay < 0) createDelay = 0;
        }

        float speed = 40; // Amount of pixels to increment by
        float targetPixelX = targetX * App.CELL_SIZE; // Convert grid coordinates to pixel coordinates
        float targetPixelY = targetY * App.CELL_SIZE;

        if (Math.abs(drawX - targetPixelX) > speed) {
            drawX += Math.signum(targetPixelX - drawX) * speed; // Increments drawX towards targetPixelX
        } else {
            drawX = targetPixelX;
        }

        if (Math.abs(drawY - targetPixelY) > speed) {
            drawY += Math.signum(targetPixelY - drawY) * speed; // Increments drawY towards targetPixelY
        } else {
            drawY = targetPixelY;
        }

        if (drawX == targetPixelX && drawY == targetPixelY) {
            moving = false; // End of animation
        }
    }

    // RENDERING

    /**
     * Adds a background for every cell. If the mouse is hovering over an empty cell it is highlighted.
     */
    public void drawBackground(App app) {
        boolean isHovered = app.mouseX > X * App.CELL_SIZE && app.mouseX < (X + 1) * App.CELL_SIZE
                && app.mouseY > Y * App.CELL_SIZE && app.mouseY < (Y + 1) * App.CELL_SIZE;

        float backgroundSize = App.CELL_SIZE - 2 * MARGIN;

        PImage backgroundImage = isHovered ? app.hoverImage : app.notHoverImage; // Chooses image
        app.image(backgroundImage, X * App.CELL_SIZE + MARGIN, Y * App.CELL_SIZE + MARGIN, backgroundSize, backgroundSize);
    }

    /**
     * Draws the value tiles over the background.
     * Only draws if a cell's value > 0 or if it is moving, and it doesn't have a createDelay.
     */
    public void drawTile(App app) {
        if ((this.value != 0 || moving) && createDelay == 0) {
            float size = App.CELL_SIZE - 2 * MARGIN;
            float pixelX = drawX + MARGIN;
            float pixelY = drawY + MARGIN;

            PImage tile = app.TILE_IMAGES.get(this.value);
            if (tile != null) {
                app.image(tile, pixelX, pixelY, size, size);
            }
        }
    }

    /**
     * Places new random tiles on an empty cell.
     */
    public void place() {
        if (this.value == 0) {
            this.value = (App.random.nextInt(2) + 1) * 2;
        }
    }
}
