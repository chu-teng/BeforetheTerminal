package dev.hae.btt.rotation;








public enum RotationStrength {
    OFF(0.0, 0.0, 0.0),
    LIGHT(1.0, 0.6, 1.5),
    MID(1.5, 0.4, 2.0),
    STRONG(2.0, 0.2, 4.0);

    private final double gain;
    private final double floor;
    private final double ceiling;

    RotationStrength(double gain, double floor, double ceiling) {
        this.gain = gain;
        this.floor = floor;
        this.ceiling = ceiling;
    }


    public double gain() {
        return gain;
    }


    public double floor() {
        return floor;
    }


    public double ceiling() {
        return ceiling;
    }

    public boolean enabled() {
        return this != OFF;
    }
}
