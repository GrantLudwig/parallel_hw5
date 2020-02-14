/*
 * Grant Ludwig
 * CPSC 5600, Seattle University
 * GeneralScan.java
 */

import java.util.ArrayList;
import java.util.List;
import java.lang.Math;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * General Scan class
 * Adapted from C++ version by Kevin Lundeen
 * @param <ElemType>
 * @param <TallyType>
 * @param <ResultType>
 */
public class GeneralScan<ElemType, TallyType, ResultType> {
    private ElemType[] data;
    private Object[] interior; // actually TallyType
    private boolean reduced;
    private int n; // size of data
    private int height;
    private int n_threads;
    private ForkJoinPool forkPool;

    private static final int ROOT = 0;

    /**
     * Constructor
     * @param raw data being passed in
     * @param n_threads
     */
    public GeneralScan(final ElemType[] raw, int n_threads) {
        this.reduced = false;
        this.n = raw.length;
        this.data = raw;
        this.height = (int) Math.ceil(Math.log(n) / Math.log(2));
        this.n_threads = n_threads;
        forkPool = new ForkJoinPool(n_threads);

        if (1 << height != n)
            throw new IllegalArgumentException("Data size must be power of 2 for now");
        interior = new Object[n_threads * 2]; // O(P*2) -> O(P)
    }

    /**
     * Runs a parallel reduce
     * @param i
     * @return
     */
    public ResultType getReduction(int i) {
        if (i >= size())
            throw new IllegalArgumentException("Non-existant node");
        if (!reduced) {
            forkPool.invoke(new ReduceRecurse(i));
            reduced = true;
        }
        return gen(value(i));
    }

    /**
     * Runs a parallel scan
     * @param output
     */
    public void getScan(ResultType output[]) {
        if (!reduced) {
            forkPool.invoke(new ReduceRecurse(ROOT));
            reduced = true;
        }
        forkPool.invoke(new ScanRecurse(ROOT, init(), output));
    }

    /**
     * Must be overloaded in class that extends
     * @return TallyType
     */
    protected TallyType init() {
        throw new UnsupportedOperationException();
    }

    /**
     * Must be overloaded in class that extends
     * @param datum ElemType
     * @return TallyType
     */
    protected TallyType prepare(final ElemType datum){
        throw new UnsupportedOperationException();
    }

    /**
     * Must be overloaded in class that extends
     * @param left TallyType
     * @param right TallyType
     * @return TallyType
     */
    protected TallyType combine(final TallyType left, final TallyType right) {
        throw new UnsupportedOperationException();
    }

    /**
     * Must be overloaded in class that extends
     * @param tally TallyType
     * @return ResultType
     */
    protected ResultType gen(final TallyType tally) {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets the value of the node at that index
     * @param i int
     * @return TallyType
     */
    private TallyType value(int i) {
        if (i < n - 1) {
            @SuppressWarnings("unchecked")
            final TallyType temp = (TallyType) interior[i];
            return temp;
        }

        else
            return prepare(data[i - (n - 1)]);
    }

    private int size() {
        return (n - 1) + n;
    }

    private int parent(int i) {
        return (i - 1) / 2;
    }

    private int left(int i) {
        return i * 2 + 1;
    }

    private int right(int i) {
        return left(i) + 1;
    }

    private boolean isLeaf(int i) {
        return left(i) >= size();
    }

    private int leftmost(int i) {
        while (!isLeaf(i))
            i = left(i);
        return i;
    }

    private int rightmost(int i) {
        while (!isLeaf(i))
            i = right(i);
        return i;
    }

    /**
     * Class for running the parallel reduce using ForkPools
     */
    private class ReduceRecurse extends RecursiveAction {
        private int node;

        /**
         * Constructor
         * @param i int, the node to reduce
         */
        public ReduceRecurse(int i) {
            this.node = i;
        }

        @Override
        protected void compute() {
            if (!isLeaf(node)) {
                if (node < n_threads - 1) {
                    // setup left
                    ReduceRecurse left = new ReduceRecurse(left(node));
                    left.fork();

                    // run right
                    ReduceRecurse right = new ReduceRecurse(right(node));
                    right.compute();

                    // join both
                    left.join();
                    interior[node] = combine(value(left(node)), value(right(node)));
                }
                else { // Runs schwartz
                    TallyType tally = init();
                    int rm = rightmost(node);
                    for (int i = leftmost(node); i <= rm; i++)
                        tally = combine(tally, value(i));
                    interior[node] = tally;
                }
            }
        }
    }

    /**
     * Class for running the parallel scan using ForkPools
     */
    private class ScanRecurse extends RecursiveAction {
        private int node;
        private TallyType tallyPrior;
        private ResultType output[];

        /**
         * Constructor
         * @param i int, the node to scan from
         * @param tallyPrior TallyType
         * @param output ResultType
         */
        public ScanRecurse(int i, TallyType tallyPrior, ResultType output[]) {
            this.node = i;
            this.tallyPrior = tallyPrior;
            this.output = output;
        }

        @Override
        protected void compute() {
            if (node < n_threads - 1) {
                // setup left
                ScanRecurse left = new ScanRecurse(left(node), tallyPrior, output);
                left.fork();

                // run right
                ScanRecurse right = new ScanRecurse(right(node), combine(tallyPrior, value(left(node))), output);
                right.compute();

                left.join();
            }
            else { // Runs schwartz
                TallyType tally = tallyPrior;
                int rm = rightmost(node);
                for (int i = leftmost(node); i <= rm; i++) {
                    tally = combine(tally, value(i));
                    output[i - (n - 1)] = gen(tally);
                }
            }
        }
    }
}