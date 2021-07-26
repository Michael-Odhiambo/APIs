
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.application.Platform;

/**
 * A MosaicCanvas object represents a grid containing rows and columns of colored rectangles. There can be "grouting"
 * between the rectangles. The grouting is just drawn as a one-pixel outline around each rectangle. The rectangles
 * can optionally be draws as raised 3D-style rectangles; this effect works much better with some colors than with
 * others. Methods are provided for getting and setting the colors of the rectangles.
 */

public class MosaicCanvas extends Canvas {

    public static void main( String[] args ) {

    }

    private int rows;   // The number of rows of rectangles in the grid.
    private int columns;  // The number of columns of rectangles in the grid.
    private Color defaultColor;  // Color used for any rectangle whose color has not been set explicitly. This can never
                                 // be null.

    private Color groutingColor;   // The color for "grouting" between rectangles. If this is null, no grouting is
                                   // drawn.

    private boolean alwaysDrawGrouting;  // Grouting is drawn around default-colored rectangles if this is true.

    private boolean use3D = true;  // If true, 3D rectangles are used; if false, flat rectangles are used to draw
                                   // rectangles.

    private boolean autopaint = true;  // If true, then when a square's color is set, repaint is called automatically.

    /**
     * An array that contains the rectangles' colors. If a null occurs in this array, the rectangle is drawn in the
     * default color, and "grouting" will be drawn around that rectangle only if alwaysDrawGrouting is true. Also, the
     * rectangle is drawn as a flat rectangle rather than as a 3D rectangle.
     */
    private Color[][] grid;

    private GraphicsContext g;  // The graphics context for drawing on this canvas.


    /**
     * Construct a MosaicPanel with 42 rows and 42 columns of rectangles, and with preferred rectangle height and width
     * both set to 16.
     */
    public MosaicCanvas() {
        this( 42, 42 );

    }

    /**
     * Construct a MosaicPanel with specified numbers of rows and columns of rectangles, and with preferred rectangle
     * height and width both set to 16.
     */
    public MosaicCanvas( int rows, int columns ) {
        this( rows, columns, 16, 16 );

    }

    /**
     * Construct a MosaicPanel with the specified number of rows and columns of rectangles, and a specified preferred
     * size for the rectangle. The default rectangle color is black, the grouting color is gray, and
     * alwaysDrawGrouting is set to false. If a non-null border color is specified, then a border of that color is
     * added to the panel, and its width is taken into account in the computation of the preferred size of the panel.
     *
     * @param rows the mosaic will have this many rows of rectangles. This must be a positive number.
     * @param columns the mosaic will have this many columns of rectangles. This must be a positive number.
     * @param preferredBlockHeight the preferred height of the mosaic will be set to this value times the number of
     *                             rows. The actual height is set by the component that contains the mosaic, and so
     *                             might not be equal to the preferred height. Size is measured in pixels. the value
     *                             should not be less than about 5, and any smaller value will be increased to 5.
     * @param preferredBlockWidth the preferred width of the mosaic will be set to this value times the number of
     *                            columns. The actual width is set by the component that contains the mosaic, and so
     *                            might not be equal to the preferred width. Size is measured in pixels. The value
     *                            should not be less than about 5, and any smaller value will be increased to 5.
     */
    public MosaicCanvas( int rows, int columns, int preferredBlockHeight, int preferredBlockWidth ) {
        this.rows = rows;
        this.columns = columns;

        if ( rows <= 0 || columns <= 0 )
            throw new IllegalArgumentException( "Rows and Columns must be greater than zero." );

        preferredBlockHeight = Math.max( preferredBlockHeight, 5 );
        preferredBlockWidth = Math.max( preferredBlockWidth, 5 );
        grid = new Color[ rows ][ columns ];

        defaultColor = Color.BLACK;
        groutingColor = Color.GRAY;
        alwaysDrawGrouting = false;

        setWidth( preferredBlockWidth*columns );
        setHeight( preferredBlockHeight*rows );

        g = getGraphicsContext2D();

    }

    // ---------------------- Methods for getting and setting grid properties. ------------------

    /**
     * Set the defaultColor. If c is null, the color will be set to black. When a mosaic is first created, the default
     * color is black. This is the color that is used for rectangles whose color value is null. Such rectangles are
     * always drawn as flat rather than 3D rectangles.
     */
    public void setDefaultColor( Color c ) {
        if ( c == null )
            c = Color.BLACK;

        if ( ! c.equals( defaultColor ) ) {
            defaultColor = c;
            forceRedraw();

        }
    }

    /**
     * Return the defaultColor, which cannot be null.
     */
    public Color getDefaultColor() {
        return defaultColor;

    }

    /**
     * Set the color of the "grouting" that is drawn between rectangles. If the value is null, no grouting is drawn
     * and the rectangles fill the entire grid. When a mosaic is first created, the groutingColor is gray.
     */
    public void setGroutingColor( Color c ) {
        if ( c == null || ! c.equals( groutingColor ) ) {
            groutingColor = c;
            forceRedraw();

        }
    }

    /**
     * Get the current groutingColor, which can be null.
     */
    public Color getGroutingColor( Color c ) {
        return groutingColor;

    }

    /**
     * Set the value of alwaysDrawGrouting. If this is false, then no grouting is drawn around rectangles whose color
     * is null. When a mosaic is first created, the value is false.
     */
    public void setAlwaysDrawGrouting( boolean always ) {
        if ( alwaysDrawGrouting != always ) {
            alwaysDrawGrouting = always;
            forceRedraw();

        }
    }

    /**
     * Get the value of the use3D property.
     */
    public boolean getUse3D() {
        return use3D;

    }

    /**
     * Set the use3D property. When this property is true, the rectangles are drawn as "3D" rectangles, which are
     * supposed to appear to be raised. When use3D is false, they are drawn as regular "flat" rectangles. Note that flat
     * rectangles are always used for background squares that have not been assigned a color. The default value of
     * use3D is true;
     */
    public void setUse3D( boolean use3D ) {
        if ( this.use3D != use3D )
            this.use3D = use3D;

    }

    /**
     * Get the value of the alwaysDrawGrouting property.
     */
    public boolean getAlwaysDrawGrouting() {
        return alwaysDrawGrouting;

    }

    /**
     * Set the number of rows and columns in the grid. If the value of the preserveData parameter is false, then the
     * color values of all the rectangles in the new grid are set to null. If it is true, then as much color data as
     * will fit is copied from the old grid. The number of rows and number of columns must be positive.
     */
    public void setGridSize( int rows, int columns, boolean preserveData ) {
        if ( rows <= 0 || columns <= 0 )
            throw new IllegalArgumentException( "Rows and columns must be positive." );

        Color[][] newGrid = new Color[ rows ][ columns ];

        if ( preserveData ) {
            int rowMax = Math.min( rows, this.rows );
            int colMax = Math.min( columns, this.columns );

            for ( int r = 0; r < rowMax; r++ )
                for ( int c = 0; c < colMax; c++ )
                    newGrid[r][c] = grid[r][c];



        }

        grid = newGrid;
        this.rows = rows;
        this.columns = columns;
        forceRedraw();

    }

    /**
     * Return the number of rows of rectangles in the grid.
     */
    public int getRowCount() {
        return rows;

    }

    /**
     * Return the number of columns of rectangles in the grid.
     */
    public int getColumnCount() {
        return columns;

    }


    // --------------------------------------- Other useful public methods. -----------------------------------------

    /**
     * Get the color which has been set for the rectangle in the specified row and column of the grid. This value can
     * be null if no color has been set for that rectangle. ( Such rectangles are actually displayed using the
     * defaultColor. ) If the specified rectangle is outside the grid, then null is returned.
     */
    public Color getColor( int row, int col ) {
        if ( row >= 0 && row < rows && col >= 0 && col < columns )
            return grid[ row ][ col ];
        else
            return null;

    }

    /**
     * Return the red component of the color of the rectangle in the specified row and column, as a double value in
     * the range 0.0 to 1.0. If the specified rectangle lies outside the grid or if no color has been specified for the
     * rectangle, then the red component of the defaultColor is returned.
     */
    public double getRed( int row, int col ) {
        if ( row >= 0 && row < rows && col >= 0 && col < columns && grid[ row ][ col ] != null )
            return grid[ row ][ col ].getRed();
        else
            return defaultColor.getRed();
    }

    /**
     * Return the green component of the color of the rectangle in the specified row and column, as a double value in
     * the range 0.0 to 1.0. If the specified rectangle lies outside the grid or if no color has been specified for the
     * rectangle, then the green component of the defaultColor is returned.
     */
    public double getGreen( int row, int col ) {
        if ( row >= 0 && row < rows && col >= 0 && col < columns && grid[ row ][ col ] != null )
            return grid[ row ][ col ].getGreen();
        else
            return defaultColor.getGreen();
    }

    /**
     * Return the blue component of the color of the rectangle in the specified row and column, as a double value in
     * the range 0.0 to 1.0. If the specified rectangle lies outside the grid or if no color has been specified for the
     * rectangle, then the blue component of the defaultColor is returned.
     */
    public double getBlue( int row, int col ) {
        if ( row >= 0 && row < rows && col >= 0 && col < columns && grid[ row ][ col ] != null )
            return grid[ row ][ col ].getBlue();
        else
            return defaultColor.getBlue();

    }

    /**
     * Set the color of the rectangle in the specified row and column. If the rectangle lies outside the grid, this is
     * simply ignored. The color can be null. Rectangles for which the color is null will be displayed in the
     * defaultColor, and they will always be shows as flat rather than 3D rects.
     */
    public void setColor( int row, int col, Color c ) {
        if ( row >= 0 && row < rows && col >= 0 && col < columns ) {
            grid[ row ][ col ] = c;
            drawSquare( row, col );

        }
    }

    /**
     * Set the color of the rectangle in the specified row and column, where the RGB color components are given as
     * double values in the range 0.0 to 1.0. Values are clamped to lie in that range. If the rectangle lies outside
     * the grid, this is simply ignored.
     */
    public void setColor( int row, int col, double red, double green, double blue ) {
        if ( row >= 0 && row < rows && col >= 0 && col < columns ) {
            red = ( red < 0 ) ? 0 : ( ( red > 1 ) ? 1 : red );
            green = ( green < 0 ) ? 0 : ( ( green > 1 ) ? 1 : green );
            blue = ( blue < 0 ) ? 0 : ( ( blue > 1 ) ? 1 : blue );

            grid[row][col] = Color.color( red, green, blue );
            drawSquare( row, col );

        }
    }


}
