package fi.jakojaannos.syvyys.entities;

import fi.jakojaannos.syvyys.GameState;

public interface HasCharacterInput {
    CharacterInput input();

    void input(CharacterInput input);

    default boolean inputDisabled(final GameState gameState) {
        return false;
    }

    void disableInput();

    void enableInput();
}
