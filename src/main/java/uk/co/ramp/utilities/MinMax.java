package uk.co.ramp.utilities;

public class MinMax {

    private final int min;
    private final int max;

    public MinMax(int a, int b) {
        min = Math.min(a, b);
        max = Math.max(a, b);
    }


    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }
}
