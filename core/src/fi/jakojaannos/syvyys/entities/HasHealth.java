package fi.jakojaannos.syvyys.entities;

public interface HasHealth {
    void dealDamage(float amount);

    float maxHealth();

    float health();
}
