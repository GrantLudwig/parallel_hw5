/*
 * Grant Ludwig
 * CPSC 5600, Seattle University
 * HeatMapDriver.java
 */

import java.io.*;
import java.util.ArrayList;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;

import javax.swing.JButton;
import javax.swing.JFrame;

/**
 * Driver Class for HeatMap
 */
public class HeatMapDriver {
    private static final int NUM_THREADS = 16;
    private static final String FILENAME = "observation_test.dat";
    private static final int DIM = 128;

    private static final String REPLAY = "Replay";
    private static JFrame application;
    private static JButton button;
    private static Color[][] grid;
    private static Grid[] scanData;

    private static int numTime; // Nnumber of time steps in observations
    private static int maxValue; // The max count value of the final grid

    /**
     * @param args First arg is the file you want to read from, by defualt the file read is observation_test.dat
     * @throws FileNotFoundException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws FileNotFoundException, InterruptedException {
        String fileName;
        if (args.length < 1)
            fileName = FILENAME;
        else
            fileName = args[0];

        HeatMap map = setupHeatMap(fileName);

        System.out.println("Running Scan on data");
        scanData = new Grid[map.dataSize()];
        map.getScan(scanData);

        maxValue = scanData[scanData.length - 1].maxCount();

        grid = new Color[DIM][DIM];
        application = new JFrame();
        application.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fillGrid(grid, 0);

        ColoredGrid gridPanel = new ColoredGrid(grid);
        application.add(gridPanel, BorderLayout.CENTER);

        button = new JButton(REPLAY);
        button.addActionListener(new BHandler());
        application.add(button, BorderLayout.PAGE_END);

        application.setSize(DIM * 4, (int)(DIM * 4.4));
        application.setVisible(true);
        application.repaint();
        System.out.println("Animating data");
        animate();
    }

    /**
     * Reads from the file and returns a HeatMap
     * @param fileName
     * @return HeatMap
     */
    private static HeatMap setupHeatMap(String fileName) {
        ArrayList<ArrayList<Observation>> observations = new ArrayList<ArrayList<Observation>>();
        System.out.println("Reading from " + fileName);
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
            System.out.println("reading from " + fileName + "failed: " + e);
            e.printStackTrace();
            System.exit(1);
        }

        Observation[][] observationArray = new Observation[observations.size()][];
        for (int i = 0; i < observations.size(); i++) {
            observationArray[i] = observations.get(i).toArray(new Observation[observations.get(i).size()]);
        }

        numTime = observations.size();
        // numThreads used must be less than the size of the data being computed
        int numThreads = NUM_THREADS;
        if (numThreads > numTime)
            numThreads = numTime;

        return new HeatMap(observationArray, numThreads, DIM);
    }

    /**
     * Animates the HeatMap
     * @throws InterruptedException
     */
    private static void animate() throws InterruptedException {
        button.setEnabled(false);
        for (int i = 0; i < numTime; i++) {
            fillGrid(grid, i);
            application.repaint();
            Thread.sleep(50);
        }
        button.setEnabled(true);
        application.repaint();
    }

    /**
     * Handles replaying the animation
     */
    static class BHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (REPLAY.equals(e.getActionCommand())) {
                new Thread() {
                    public void run() {
                        try {
                            animate();
                        } catch (InterruptedException e) {
                            System.exit(0);
                        }
                    }
                }.start();
            }
        }
    };

    static private final Color COLD = new Color(0x0a, 0x37, 0x66), HOT = Color.RED;
    static private int offset = 0;

    /**
     * Fills the color grid
     * @param grid
     * @param timeNum
     */
    private static void fillGrid(Color[][] grid, int timeNum) {
        for (int r = 0; r < grid.length; r++)
            for (int c = 0; c < grid[r].length; c++)
                grid[r][c] = interpolateColor((double) scanData[timeNum].grid[r][c] / maxValue, COLD, HOT);
    }

    /**
     * Interpolates the color to put on the pixel
     * @param ratio
     * @param a
     * @param b
     * @return
     */
    private static Color interpolateColor(double ratio, Color a, Color b) {
        int ax = a.getRed();
        int ay = a.getGreen();
        int az = a.getBlue();
        int cx = ax + (int) ((b.getRed() - ax) * ratio);
        int cy = ay + (int) ((b.getGreen() - ay) * ratio);
        int cz = az + (int) ((b.getBlue() - az) * ratio);
        return new Color(cx, cy, cz);
    }
}