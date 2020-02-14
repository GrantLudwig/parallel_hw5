public class Grid {
    public int[][] grid;
    public int size;

    public Grid(int size) {
        this.grid = new int[size][size];
        this.size = size;
    }

    /**
     * Returns the sum of the grid and the passed in right grid
     * @param right
     * @return
     */
    public Grid sum(Grid right) {
        if (this.size != right.size)
            throw new IllegalArgumentException("Grids must be same size");
        Grid sumGrid = new Grid(this.size);
        for (int i = 0; i < this.size; i++) {
            for (int j = 0; j < this.size; j++) {
                sumGrid.grid[i][j] = this.grid[i][j] + right.grid[i][j];
            }
        }
        return sumGrid;
    }

    public String toString() {
        String output = "";
        for (int i = 0; i < size; i++) { // y
            String line = "";
            for (int j = 0; j < size; j++) { // x
                line += grid[j][i] + " ";
            }
            output += "[ " + line + "]\n";
        }
        return "Grid:\n" + output + "\n";
    }
}