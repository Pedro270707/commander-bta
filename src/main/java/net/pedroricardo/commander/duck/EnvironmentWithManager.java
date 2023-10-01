package net.pedroricardo.commander.duck;

import net.pedroricardo.commander.content.CommanderCommandManager;

public interface EnvironmentWithManager {
    CommanderCommandManager getManager();
}
