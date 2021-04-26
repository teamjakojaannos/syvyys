package fi.jakojaannos.syvyys.entities;

public interface TracksPlayerContact {
    void beginContact();

    void endContact();

    class Simple implements TracksPlayerContact {
        public boolean isInContactWithPlayer;

        @Override
        public void beginContact() {
            this.isInContactWithPlayer = true;
        }

        @Override
        public void endContact() {
            this.isInContactWithPlayer = false;
        }
    }
}
