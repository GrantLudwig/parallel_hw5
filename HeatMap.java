/*
 * Grant Ludwig
 * CPSC 5600, Seattle University
 * HeatMap.java
 */

import java.io.*;
import java.util.ArrayList;

/**
 * HeatMap Class
 * Generates a set of Grids, represtening a HeatMap, for each time
 * Extends GeneralScan
 */
public class HeatMap extends GeneralScan<Observation[], Grid, Grid>

    private int DIM;
    private int dataSize;

    /**
     * Constructor
     * @param data Observation[][], 2D array of Observations
     *              indexed first by times, then by Observations in that time
     * @param numThreads int
     * @param DIM int
     */
    public HeatMap(Observation data[][], int numThreads, int DIM) {
        super(data, numThreads);
        this.DIM = DIM;
        this.dataSize = data.length;
    }

    public int dataSize() {
        return dataSize;
    }

    @Override
    protected Grid init() {
        return new Grid(DIM);
    }

    /**
     * Converts an array of Observations into a Grid
     * @param datum
     * @return
     */
    @Override
    protected Grid prepare(final Observation[] datum) {

        Grid temp = new Grid(DIM);
        double sectionInterval = 2.0 / DIM;

        // loop through each
        for (int i = 0; i < datum.length; i++) {
            int     xSection = -1,
                    ySection = -1;
            double sectionStart = -1.0;
            double sectionEnd = 0;
            int section = 0;

            // find x position on grid
            while (xSection == -1) {
                sectionEnd = sectionStart + sectionInterval;
                // check if this should be the final interval
                if (section == DIM - 1)
                    sectionEnd = 1.0;
                if (sectionStart <= datum[i].x && datum[i].x <= sectionEnd)
                    xSection = section;
                sectionStart += sectionInterval;
                section++;
            }

            // find y position on grid
            sectionStart = -1.0;
            sectionEnd = 0;
            section = 0;
            while (ySection == -1) {
                sectionEnd = sectionStart + sectionInterval;
                // check if this should be the final interval
                if (section == DIM - 1)
                    sectionEnd = 1.0;
                if (sectionStart <= datum[i].y && datum[i].y <= sectionEnd)
                    ySection = section;
                sectionStart += sectionInterval;
                section++;
            }

            // set grid and return
            temp.grid[xSection][ySection] += 1;
        }
        return temp;
    }

    /**
     * Adds the two Grids together
     * @param left
     * @param right
     * @return Grid
     */
    @Override
    protected Grid combine(final Grid left, final Grid right) {
        return left.sum(right);
    }

    @Override
    protected Grid gen(final Grid tally) {
        return tally;
    }