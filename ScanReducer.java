/*
 * Grant Ludwig
 * CPSC 5600, Seattle University
 * General implementation and tester of General Scan
 */

public class ScanReducer extends GeneralScan<Integer, Integer, Integer> {
    public ScanReducer(Integer data[]) {
        super(data, 16);
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
        Integer[] data = new Integer[16];
        for (int i = 0; i < 16; i++) {
            data[i] = 1;
        }

        ScanReducer test = new ScanReducer(data);
        System.out.println(test.getReduction(0));
    }
}