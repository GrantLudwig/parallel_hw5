/*
 * Grant Ludwig
 * CPSC 5600, Seattle University
 * General implementation and tester of General Scan
 */

public class ScanReducer extends GeneralScan<Integer, Integer, Integer> {
    private static final int N = 32;
    private static final int NUM_THREADS = 16;

    public ScanReducer(Integer data[]) {
        super(data, NUM_THREADS);
    }

    @Override
    protected Integer init() {
        return 0;
    }

    @Override
    protected Integer prepare(final Integer datum){
        return datum;
    }

    @Override
    protected Integer combine(final Integer left, final Integer right) {
        return left + right;
    }

    @Override
    protected Integer gen(final Integer tally) {
        return tally;
    }

    public static void main(String[] args) {
        Integer[] data = new Integer[N];
        for (int i = 0; i < N; i++) {
            data[i] = 1;
        }

        ScanReducer test = new ScanReducer(data);
        System.out.println(test.getReduction(0));
        Integer[] scanData = new Integer[N];
        test.getScan(scanData);
        int testInt = 1;
        for (int i = 0; i < scanData.length; i++){
            if (scanData[i] != testInt)
                System.out.println("Failed");
            System.out.print(scanData[i] + " ");
            testInt++;
        }
    }
}