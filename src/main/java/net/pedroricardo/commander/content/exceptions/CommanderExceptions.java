package net.pedroricardo.commander.content.exceptions;

import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.core.lang.I18n;

public class CommanderExceptions {
    private static final SimpleCommandExceptionType INCOMPLETE_ARGUMENT = new SimpleCommandExceptionType(() -> I18n.getInstance().translateKey("exceptions.commander.incomplete"));
    private static final SimpleCommandExceptionType NOT_IN_WORLD = new SimpleCommandExceptionType(() -> I18n.getInstance().translateKey("exceptions.commander.not_in_world"));
    private static final SimpleCommandExceptionType EMPTY_SELECTOR = new SimpleCommandExceptionType(() -> I18n.getInstance().translateKey("argument_types.commander.entity.invalid_selector.empty"));
    private static final SimpleCommandExceptionType INVALID_SELECTOR = new SimpleCommandExceptionType(() -> I18n.getInstance().translateKey("argument_types.commander.entity.invalid_selector.generic"));
    private static final SimpleCommandExceptionType SINGLE_ENTITY_ONLY = new SimpleCommandExceptionType(() -> I18n.getInstance().translateKey("argument_types.commander.entity.invalid_selector.single_entity_only"));
    private static final SimpleCommandExceptionType SINGLE_PLAYER_ONLY = new SimpleCommandExceptionType(() -> I18n.getInstance().translateKey("argument_types.commander.entity.invalid_selector.single_player_only"));
    private static final SimpleCommandExceptionType PLAYER_ONLY = new SimpleCommandExceptionType(() -> I18n.getInstance().translateKey("argument_types.commander.entity.invalid_selector.player_only"));
    private static final SimpleCommandExceptionType SINGLE_PLAYER_WORLD_ONLY = new SimpleCommandExceptionType(() -> I18n.getInstance().translateKey("exceptions.commander.single_player_world_only"));
    private static final SimpleCommandExceptionType MULTIPLAYER_WORLD_ONLY = new SimpleCommandExceptionType(() -> I18n.getInstance().translateKey("exceptions.commander.multiplayer_world_only"));
    private static final SimpleCommandExceptionType VOLUME_TOO_LARGE = new SimpleCommandExceptionType(() -> I18n.getInstance().translateKey("exceptions.commander.volume_too_large"));

    public static SimpleCommandExceptionType incomplete() {
        return INCOMPLETE_ARGUMENT;
    }

    public static SimpleCommandExceptionType notInWorld() {
        return NOT_IN_WORLD;
    }

    public static SimpleCommandExceptionType emptySelector() {
        return EMPTY_SELECTOR;
    }

    public static SimpleCommandExceptionType invalidSelector() {
        return INVALID_SELECTOR;
    }

    public static SimpleCommandExceptionType singleEntityOnly() {
        return SINGLE_ENTITY_ONLY;
    }

    public static SimpleCommandExceptionType singlePlayerOnly() {
        return SINGLE_PLAYER_ONLY;
    }

    public static SimpleCommandExceptionType playerOnly() {
        return PLAYER_ONLY;
    }

    public static SimpleCommandExceptionType singlePlayerWorldOnly() {
        return SINGLE_PLAYER_WORLD_ONLY;
    }

    public static SimpleCommandExceptionType multiplayerWorldOnly() {
        return MULTIPLAYER_WORLD_ONLY;
    }

    public static SimpleCommandExceptionType volumeTooLarge() {
        return VOLUME_TOO_LARGE;
    }
}
