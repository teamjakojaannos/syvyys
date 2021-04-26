package fi.jakojaannos.syvyys.renderer;

public enum RenderLayer {
    BACKGROUND,
    MAIN,
    FOREGROUND;

    public static RenderLayer[] backToFront() {
        return new RenderLayer[]{
                BACKGROUND,
                MAIN,
                FOREGROUND
        };
    }
}
