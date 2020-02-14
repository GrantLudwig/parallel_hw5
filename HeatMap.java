/*
 * Grant Ludwig
 * CPSC 5600, Seattle University
 * HeatMap.java
 */

import java.io.*;
import java.util.ArrayList;

public class HeatMap extends GeneralScan<Observation[], Grid, Grid> {
    private static final int NUM_THREADS = 16;
    private static final String FILENAME = "observation_test.dat";
    private static final int GRID_SIZE = 16;
    private static final int N = 256;

    private int DIM;

    public HeatMap(Observation data[][], int numThreads, int DIM) {
        super(data, numThreads);
        this.DIM = DIM;
    }

    @Override
    protected Grid init() {
        return new Grid(DIM);
    }

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

    @Override
    protected Grid combine(final Grid left, final Grid right) {
        return left.sum(right);
    }

    @Override
    protected Grid gen(final Grid tally) {
        return tally;
    }

    public static void main(String[] args) {
        String fileName = "";
        ArrayList<ArrayList<Observation>> observations = new ArrayList<ArrayList<Observation>>();
        if (args.length < 1)
            fileName = FILENAME;
        else
            fileName = args[0];

        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName));
            Observation obs = (Observation) in.readObject();
            int timeNum = 0;
            while (!obs.isEOF()) {
                observations.add(new ArrayList<Observation>());
                while (timeNum == obs.time){
                    observations.get(timeNum).add(obs);
                    obs = (Observation) in.readObject();
                }
                timeNum++;
            }
            in.close();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("reading from " + FILENAME + "failed: " + e);
            e.printStackTrace();
            System.exit(1);
        }

        Observation[][] observationArray = new Observation[observations.size()][];
        for (int i = 0; i < observations.size(); i++) {
            observationArray[i] = observations.get(i).toArray(new Observation[observations.get(i).size()]);
        }

        // numThreads used must be less than the size of the data being computed
        int numThreads = NUM_THREADS;
        if (numThreads > observations.size())
            numThreads = observations.size();
        HeatMap test = new HeatMap(observationArray, numThreads, GRID_SIZE);

        //HeatMap test = new HeatMap(observations.toArray(new Observation[observations.size()]));
        System.out.println(test.getReduction(0));

        Grid[] scanData = new Grid[observations.size()];
        test.getScan(scanData);
        System.out.println("Scan Data");
        for (int i = 0; i < scanData.length; i++){
            System.out.println("Time " + i + " " +scanData[i]);
        }
    }
}