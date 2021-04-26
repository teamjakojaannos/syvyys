package fi.jakojaannos.syvyys;

import fi.jakojaannos.syvyys.entities.Player;

import java.util.function.Supplier;

public record Upgrade(int iconIndex, int cost, Label label, Action action, Supplier<Upgrade[]> unlocks) {
    public Upgrade(final int iconIndex, final int cost, final Label label, final Action action) {
        this(iconIndex, cost, label, action, () -> new Upgrade[0]);
    }

    public static Upgrade[] getAllInitialUpgrades() {
        // HACK: font has no "+" character. "x" looks a bit like "+" so use that
        // HACK: font has no ":" character. ";" looks a bit like ":" so use that
        return new Upgrade[]{
                new Upgrade(1, 100, (s, p) -> "Ability; dash", (s, p) -> p.dashUnlocked = true, () -> createDashUnlocks(1)),
                new Upgrade(1, 100, (s, p) -> String.format("x1 max speed (%.1f)", p.damage), (s, p) -> p.maxSpeed += 1.0f, () -> createMaxSpeedUnlocks(1)),
                new Upgrade(0, 100, (s, p) -> String.format("x1 damage (%.1f)", p.damage), (s, p) -> p.damage += 1.0f, () -> createDamageUnlocks(1)),
                new Upgrade(0, 100, (s, p) -> String.format("x1 shot per attack (%d)", p.shotsPerAttack()), (s, p) -> p.shotsPerAttack(p.shotsPerAttack() + 1), () -> createSPAUnlocks(1)),
        };
    }

    private static Upgrade[] createDashUnlocks(final int lvl) {
        if (lvl >= 10) {
            return new Upgrade[0];
        }

        return new Upgrade[]{
                new Upgrade(1, 100 + 50 * lvl, (s, p) -> String.format("x0.1s dash duration (%.1fs)", p.dashDuration), (s, p) -> p.dashDuration += 0.1f, () -> createDashUnlocks(lvl + 1))
        };
    }

    private static Upgrade[] createMaxSpeedUnlocks(final int lvl) {
        if (lvl >= 100) {
            return new Upgrade[0];
        }

        return new Upgrade[]{
                new Upgrade(1, 100 + 100 * lvl, (s, p) -> String.format("x1 max speed (%.1f)", p.maxSpeed), (s, p) -> p.maxSpeed += 1.0f, () -> createMaxSpeedUnlocks(lvl + 1)),
        };
    }

    private static Upgrade[] createDamageUnlocks(final int lvl) {
        if (lvl >= 100) {
            return new Upgrade[0];
        }

        return new Upgrade[]{
                new Upgrade(0, 100 + 100 * lvl, (s, p) -> String.format("x1 damage (%.1f)", p.damage), (s, p) -> p.damage += 1.0f, () -> createDamageUnlocks(lvl + 1))
        };
    }

    private static Upgrade[] createSPAUnlocks(final int lvl) {
        if (lvl >= 7) {
            return new Upgrade[0];
        }

        return new Upgrade[]{
                new Upgrade(0, 100 + 300 * lvl, (s, p) -> String.format("x1 shot per attack (%d)", p.shotsPerAttack()), (s, p) -> p.shotsPerAttack(p.shotsPerAttack() + 1), () -> createSPAUnlocks(lvl + 1)),
        };
    }

    public interface Label {
        String get(GameState state, Player player);
    }

    public interface Action {
        void apply(GameState state, Player player);
    }
}
