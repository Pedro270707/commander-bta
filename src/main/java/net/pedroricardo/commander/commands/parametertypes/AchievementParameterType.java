package net.pedroricardo.commander.commands.parametertypes;

import net.minecraft.client.Minecraft;
import net.minecraft.core.achievement.Achievement;
import net.minecraft.core.achievement.AchievementList;
import net.pedroricardo.commander.mixin.StatNameAccessor;

import java.util.ArrayList;
import java.util.List;

public class AchievementParameterType extends CommandParameterType {
    public AchievementParameterType() {
        super(1);
    }

    @Override
    public List<String> getSuggestions(Minecraft mc, int parameterIndex, int localIndex, String parameter) {
        List<String> suggestions = new ArrayList<>();
        for (Achievement achievement : AchievementList.achievementList) {
            suggestions.add(((StatNameAccessor)achievement).statName());
        }
        return suggestions;
    }
}
