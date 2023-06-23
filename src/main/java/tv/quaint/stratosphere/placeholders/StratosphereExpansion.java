package tv.quaint.stratosphere.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tv.quaint.stratosphere.Stratosphere;
import tv.quaint.stratosphere.plot.PlotUtils;
import tv.quaint.stratosphere.plot.SkyblockPlot;
import tv.quaint.stratosphere.users.SkyblockUser;

import java.util.ArrayList;
import java.util.Arrays;

public class StratosphereExpansion extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "strato";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Quaint";
    }

    @Override
    public @NotNull String getVersion() {
        return Stratosphere.getInstance().getDescription().getVersion();
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        SkyblockUser user = PlotUtils.getOrGetUser(player.getUniqueId().toString());
        if (user == null) return null;

        SkyblockPlot plot = user.getOrGetPlot();
        if (plot == null) return null;

        if (params.equalsIgnoreCase("island_size_radius")) {
            return String.valueOf(plot.getRadius());
        }
        if (params.equalsIgnoreCase("island_size_diameter")) {
            return String.valueOf(plot.getRadius() * 2);
        }
        if (params.equalsIgnoreCase("island_size_area")) {
            return String.valueOf(plot.getRadius() * plot.getRadius());
        }

        if (params.equalsIgnoreCase("island_level_applied")) {
            return String.valueOf(plot.getLevel());
        }
        if (params.equalsIgnoreCase("island_level_real")) {
            return String.valueOf(plot.getRealLevel());
        }
        if (params.equalsIgnoreCase("island_level_max")) {
            return String.valueOf(plot.getMaxLevel());
        }
        if (params.equalsIgnoreCase("island_xp")) {
            return String.valueOf(plot.getXp());
        }

        if (params.equalsIgnoreCase("island_owner")) {
            return String.valueOf(plot.getOwner().getName());
        }

        if (params.equalsIgnoreCase("island_score")) {
            return String.valueOf(plot.calculateScore());
        }

        if (params.startsWith("top_")) {
            String[] split = params.split("_");

            int index;
            try {
                index = Integer.parseInt(split[1]);
            } catch (Exception e) {
                return null;
            }

            SkyblockPlot[] array = Arrays.copyOf(Stratosphere.getTopConfig().getTopScores(10).values().toArray(), 10, SkyblockPlot[].class);

            SkyblockPlot p = array[index - 1];
            if (p == null) return null;

            if (split.length == 2) {
                return p.getOwner().getName();
            } else if (split.length == 3) {
                if (split[2].equalsIgnoreCase("score")) {
                    return String.valueOf(p.calculateScore());
                }
                if (split[2].equalsIgnoreCase("owner")) {
                    return String.valueOf(p.getOwner().getName());
                }
            }
        }

        return null;
    }
}
