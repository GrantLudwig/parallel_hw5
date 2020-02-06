/*
 * Kevin Lundeen
 * CPSC 5600, Seattle University
 * This is free and unencumbered software released into the public domain.
 */

import java.io.*;

/**
 * Represents an observation from our detection device. When a location on the
 * sensor triggers, the time and the location of the detected event are recorded
 * in one of these Observation objects.
 */
public class Observation implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final long EOF = Long.MAX_VALUE;  // our convention to mark EOF with a special object

    public long time; // number of milliseconds since turning on the detector device
    public double x, y; // location of the detected event on the detection grid

    public Observation(long time, double x, double y) {
        this.time = time;
        this.x = x;
        this.y = y;
    }

    public Observation() {
        this.time = EOF;
        this.x = this.y = 0.0;
    }

    public boolean isEOF() {
        return time == EOF;
    }

    public String toString() {
        return "Observation(" + time + ", " + x + ", " + y + ")";
    }

    /**
     * Example with serialization of a series of Observation to a local file.
     *
     * @param args not used
     */
    public static void main(String[] args) {
        final String FILENAME = "observation_test.dat";

		/*
		Write some arbitrary observations to a observation data file.
		 */
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILENAME));
            for (long t = 0; t < 6; t++)
                for (double x = -0.41; x < 1.0; x += 0.194)
                    for (double y = 0.37; y > -0.9; y -= 0.423)
                        out.writeObject(new Observation(t, x, y));
            out.writeObject(new Observation());  // to mark EOF
            out.close();
        } catch (IOException e) {
            System.out.println("writing to " + FILENAME + "failed: " + e);
            e.printStackTrace();
            System.exit(1);
        }

		/*
		Now read them from the data file and display them on the console.
		 */
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(FILENAME));
            int count = 0;
            Observation obs = (Observation) in.readObject();
            while (!obs.isEOF()) {
                System.out.println(++count + ": " + obs);
                obs = (Observation) in.readObject();
            }
            in.close();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("reading from " + FILENAME + "failed: " + e);
            e.printStackTrace();
            System.exit(1);
        }
    }

}
