package fi.jakojaannos.syvyys;

public record TimerHandle(long id, float duration, Timers.Action action, boolean looping) {}
