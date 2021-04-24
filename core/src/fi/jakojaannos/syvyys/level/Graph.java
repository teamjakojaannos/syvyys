package fi.jakojaannos.syvyys.level;

import java.util.Random;

public record Graph(double frequency, double offset) {

    public double getValueAt(final double x) {
        return Math.sin(x * this.frequency + this.offset);
    }

    public static Graph randomGraph(final Random random, final double frequencyMultiplier) {
        return new Graph(
                random.nextDouble() * frequencyMultiplier,
                random.nextDouble() * 10);
    }

}
