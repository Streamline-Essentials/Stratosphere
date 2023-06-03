package tv.quaint.stratosphere.commands;

import lombok.Getter;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import tv.quaint.stratosphere.utils.MessageUtils;

import java.util.Objects;

public abstract class AbstractQuaintCommand implements TabExecutor {
    @Getter
    final String identifier;

    public AbstractQuaintCommand(String identifier, JavaPlugin provider) {
        this.identifier = identifier;

        try {
            Objects.requireNonNull(provider.getCommand(identifier)).setExecutor(this);
        } catch (Exception e) {
            MessageUtils.logWarning("Failed to register command '" + identifier + "'! --> No command found in plugin.yml!");
        }
    }
}
