package net.livecar.nuttyworks.npc_destinations.listeners.commands;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CommandInfo {
    String name();

    String group();

    String helpMessage();

    String languageFile();

    String[] permission();

    String[] arguments();

    boolean allowConsole();

    int minArguments();

    int maxArguments();
}
