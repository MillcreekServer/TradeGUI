package io.github.wysohn.tradegui.util;

public class NumBuilder {
    private double num = 0.0;
    private double decimal = 0.0;
    private boolean dot = false;

    private int decimalPoints = -1;

    public NumBuilder() {
        this(0.0);
    }

    public NumBuilder(double num) {
        this.num = num;
    }

    public double getNum() {
        return num + decimal;
    }

    public boolean isDot() {
        return dot;
    }

    public boolean toggleDot() {
        dot = !dot;
        if (dot) {
            decimal = 0.0;
            decimalPoints = -1;
        }
        return dot;
    }

    public void append(int digit, int decimalLimit, double floor, double ceiling) {
        if (dot && decimalPoints < -decimalLimit) {
            //all limit is filled already, so toggle back to integer mode
            dot = false;
        }

        if (dot) {
            decimal += digit * Math.pow(10.0, decimalPoints--);
        } else {
            if (digit < 1 && num == 0.0) // first digit is 0 so ignore
                return;

            num *= 10.0;
            num += digit;
        }

        double value = getNum();
        if (value >= ceiling) {
            dot = false;
            decimal = 0.0;
            decimalPoints = -1;

            num = ceiling;
        }
        if (value <= floor) {
            dot = false;
            decimal = 0.0;
            decimalPoints = -1;

            num = floor;
        }
    }

    public void append(int digit, double floor, double ceiling) {
        append(digit, 2, floor, ceiling);
    }

    public void append(int digit) {
        append(digit, 2, FLOOR, CEILING);
    }

    public void clear() {
        num = 0.0;

        dot = false;
        decimal = 0.0;
        decimalPoints = -1;
    }

    public static final double FLOOR = Double.MIN_VALUE / 2;
    public static final double CEILING = Double.MAX_VALUE / 2;
}
