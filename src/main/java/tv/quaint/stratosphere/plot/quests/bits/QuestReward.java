package tv.quaint.stratosphere.plot.quests.bits;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.Player;
import tv.quaint.stratosphere.plot.PlotUtils;
import tv.quaint.stratosphere.plot.SkyblockPlot;
import tv.quaint.stratosphere.users.SkyblockUser;

import java.util.List;

public class QuestReward {
    public enum RewardType {
        DUST,
        XP,
        LEVELS,
        COMMAND_SELF,
        COMMAND_ALL,
        COMMAND_CONSOLE,
        MESSAGE,
        ;
    }

    @Getter @Setter
    private RewardType type;
    @Getter @Setter
    private String payload;

    public QuestReward(RewardType type, String payload) {
        this.type = type;
        this.payload = payload;
    }

    public QuestReward(String from) {
        String[] split = from.split("! ", 2);

        try {
            this.type = RewardType.valueOf(split[0]);
            this.payload = split[1];
        } catch (Exception e) {
            e.printStackTrace();
            this.type = RewardType.MESSAGE;
            this.payload = "null";
        }
    }

    public double getDouble() {
        try {
            return Double.parseDouble(payload);
        } catch (Exception e) {
            e.printStackTrace();
            return 0d;
        }
    }

    public int getInt() {
        try {
            return Integer.parseInt(payload);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public boolean reward(Player finisher) {
        SkyblockUser user = PlotUtils.getOrGetUser(finisher.getUniqueId().toString());
        if (user == null) return false;

        SkyblockPlot plot = user.getOrGetPlot();
        if (plot == null) return false;

        switch (type) {
            case DUST:
                plot.getMembers().forEach(plotMember -> {
                    plotMember.getSkyblockUser().addStarDust(getDouble());
                    plotMember.getSkyblockUser().saveAll();
                });
                plot.saveAll();
                break;
            case XP:
                plot.addXp(getInt());
                plot.saveAll();
                break;
            case LEVELS:
                plot.addLevel(getInt());
                plot.saveAll();
                break;
            case COMMAND_SELF:
                finisher.performCommand(payload);
                break;
            case COMMAND_ALL:
                plot.getMembers().forEach(plotMember -> {
                    plotMember.getBukkitPlayer().performCommand(payload);
                });
                break;
            case COMMAND_CONSOLE:
                finisher.getServer().dispatchCommand(finisher.getServer().getConsoleSender(), payload);
                break;
            case MESSAGE:
                user.getStreamlineUser().sendMessage(payload);
                break;
        }

        return true;
    }
}
