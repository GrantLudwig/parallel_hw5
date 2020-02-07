/*
 * Grant Ludwig
 * CPSC 5600, Seattle University
 * General Scan
 */

import java.lang.Math;

/**
 *
 * @param <T> ElemType
 * @param <S> TallyType
 * @param <U> ResultType
 */
public class GeneralScan<T, S, U> {
    private T[] data;
    private Object[] interior; // actually type S
    private boolean reduced;
    private int n; // size of data
    private int height;
    private int n_threads;

    private static final int ROOT = 0;

    public GeneralScan(final T[] raw, int n_threads) {
        this.reduced = false;
        this.n = raw.length;
        this.data = raw;
        this.height = (int) Math.ceil(Math.log(n) / Math.log(2));
        this.n_threads = n_threads;

        if (1 << height != n)
            throw new IllegalArgumentException("Data size must be power of 2 for now");
        interior = new Object[n - 1];
    }

    public U getReduction(int i) {
        if (i >= size())
            throw new IllegalArgumentException("Non-existant node");
        reduced = reduced || reduce(ROOT);
        return gen(value(i));
    }

    public void getScan(U output[]) {
        reduced = reduced || reduce(ROOT);
        scan(ROOT, init(), output);
    }

    protected S init() {
        throw new UnsupportedOperationException();
    }

    protected S prepare(final T datum){
        throw new UnsupportedOperationException();
    }

    protected S combine(final S left, final S right) {
        throw new UnsupportedOperationException();
    }

    protected U gen(final S tally) {
        throw new UnsupportedOperationException();
    }

    private S value(int i) {
        if (i < n - 1) {
            @SuppressWarnings("unchecked")
            final S temp = (S) interior[i];
            return temp;
        }

        else
            return prepare(data[i - (n - 1)]);
    }

    private boolean reduce(int i) {
        if (!isLeaf(i)) {
//            if (i < n_threads - 1) {
//                // Threaded
//            }
//            else {
                reduce(left(i));
                reduce(right(i));
//            }
            interior[i] = combine(value(left(i)), value(right(i)));
        }
        return true;
    }

    private void scan(int i, S tallyPrior, U output[]) {
        if (isLeaf(i))
            output[i - (n - 1)] = gen(combine(tallyPrior, value(i)));
        else {
//            if (i < n_threads - 1) {
//                // Threaded
//            }
//            else {
                scan(left(i), tallyPrior, output);
                scan(right(i), combine(tallyPrior, value(left(i))), output);
//            }
        }
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
        return left(i);
    }

    private boolean isLeaf(int i) {
        return left(i) >= size();
    }
}