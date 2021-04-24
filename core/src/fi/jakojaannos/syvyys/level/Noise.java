package fi.jakojaannos.syvyys.level;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Noise {

    private static final Random VALUE_NOISE_RANDOM = new Random();
    private final List<List<Double>> octaves = new ArrayList<>();
    private final int width;


    public Noise(final Random random, final int width, final int octaves) {
        this.width = width;

        int length = 1;
        for (int n = 0; n < octaves; n++) {
            final var layer = new ArrayList<Double>();

            length = Math.min(length, width);
            for (int i = 0; i < length; i++) {
                layer.add(random.nextDouble());
            }

            this.octaves.add(layer);

            length *= 2;
        }
    }

    public double lassiValueAt(final int x) {
        double total = 0;
        double mult = 1;
        for (final var octave : this.octaves) {
            final int index = x * octave.size() / this.width;
            final double value = octave.get(index);
            total += value * mult;
            mult *= 0.5;
        }
        total /= this.octaves.size();

        return total;
    }

    public double valueAt(final int x) {
        final int nOctaves = 8;

        double totalSum = 0;
        double octaveWeight = 1.0;
        int octaveSize = 1;
        int iOctave = 0;
        while (iOctave < nOctaves) {
            double octaveSum = 0;
            for (int n = 0; n < octaveSize; n++) {
                octaveSum += generateNoiseAt(x * octaveSize);
            }

            final double weightedOctaveSum = octaveSum * octaveWeight;
            totalSum += weightedOctaveSum;
            octaveWeight /= 2.0;
            octaveSize *= 2;

            ++iOctave;
        }

        return totalSum / nOctaves;
    }

    private double generateNoiseAt(final int x) {
        VALUE_NOISE_RANDOM.setSeed(x);
        return VALUE_NOISE_RANDOM.nextDouble();
    }
}
