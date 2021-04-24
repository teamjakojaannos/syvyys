package fi.jakojaannos.syvyys.level;


import java.util.List;

public record SinNoise(List<Graph> graphs) {

    public double getSumAt(final double x) {
        double total = 0.0;
        for (final var graph : this.graphs) {
            total += graph.getValueAt(x);
        }
        return total / this.graphs.size();
    }

    public double getProductAt(final double x) {
        double total = 1.0;
        for (final var graph : this.graphs) {
            total *= graph.getValueAt(x);
        }
        return total / this.graphs.size();
    }
}
