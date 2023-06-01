package tv.quaint.stratosphere.plot.members;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import tv.quaint.stratosphere.plot.SkyblockPlot;
import tv.quaint.stratosphere.plot.flag.PlotFlag;
import tv.quaint.stratosphere.plot.pos.PlotFlagIdentifiers;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ConcurrentSkipListSet;

public class PlotRole implements Comparable<PlotRole> {
    @Getter @Setter
    private String identifier;
    @Getter @Setter
    private String name;
    @Getter @Setter
    private SkyblockPlot plot;
    @Getter @Setter
    private ConcurrentSkipListSet<PlotFlag> flags;

    public PlotRole(String identifier, String name, SkyblockPlot plot, PlotFlag... flags) {
        this.identifier = identifier;
        this.name = name;
        this.plot = plot;
        this.flags = new ConcurrentSkipListSet<>(Arrays.asList(flags));
    }

    public void addFlag(PlotFlag flag) {
        removeFlag(flag);
        flags.add(flag);
    }

    public void addFlags(PlotFlag... flags) {
        removeFlags(flags);
        this.flags.addAll(Arrays.asList(flags));
    }

    public void addFlag(String identifier, String value) {
        removeFlag(identifier);
        flags.add(new PlotFlag(identifier, value));
    }

    public void removeFlag(PlotFlag flag) {
        flags.removeIf(fl -> fl.getIdentifier().equals(flag.getIdentifier()));
    }

    public void removeFlag(String identifier) {
        flags.removeIf(fl -> fl.getIdentifier().equals(identifier));
    }

    public void removeFlags(PlotFlag... flags) {
        getFlags().removeIf(flag -> {
            String identifier = flag.getIdentifier();

            for (PlotFlag fl : flags) {
                if (fl.getIdentifier().equals(identifier)) return true;
            }

            return false;
        });
    }

    public boolean hasFlag(String identifier) {
        return hasFlag(identifier, true);
    }

    public boolean hasFlag(String identifier, boolean countParents) {
        boolean hasFlag = getSelfFlag(identifier) != null;
        if (hasFlag) return true;
        if (! countParents || Objects.equals(identifier, PlotFlagIdentifiers.PARENT.getIdentifier())) return false;

        PlotRole parent = getParent();
        int i = 0;
        while (i < 100) {
            if (parent == null) return false;
            hasFlag = parent.getSelfFlag(identifier) != null;
            if (hasFlag) return true;

            parent = parent.getParent();

            i++;
        }

        return false;
    }

    public PlotRole getParent() {
        return plot.getParentRole(this);
    }

    public PlotFlag getSelfFlag(String identifier) {
        return flags.stream().filter(f -> f.getIdentifier().equals(identifier)).findFirst().orElse(null);
    }

    public boolean hasFlag(PlotFlag flag, boolean countParents) {
        return hasFlag(flag.getIdentifier(), countParents);
    }

    public boolean hasFlag(PlotFlag flag) {
        return hasFlag(flag, true);
    }

    public PlotFlag getFlag(String identifier, boolean countParents) {
        PlotFlag flag = getSelfFlag(identifier);
        if (flag != null) return flag;
        if (! countParents || Objects.equals(identifier, PlotFlagIdentifiers.PARENT.getIdentifier())) return null;

        PlotRole parent = getParent();
        int i = 0;
        while (i < 100) {
            if (parent == null) return null;
            flag = parent.getSelfFlag(identifier);
            if (flag != null) return flag;

            parent = parent.getParent();

            i++;
        }

        return null;
    }

    public PlotFlag getFlag(String identifier) {
        return getFlag(identifier, true);
    }

    public PlotFlag getFlag(PlotFlag flag) {
        return getFlag(flag.getIdentifier());
    }

    public PlotFlag getFlag(PlotFlag flag, boolean countParents) {
        return getFlag(flag.getIdentifier(), countParents);
    }

    public boolean hasFlagValue(String identifier, String value) {
        PlotFlag flag = getFlag(identifier);
        if (flag == null) return false;
        return flag.getValue().equals(value);
    }

    public boolean hasFlagValue(String identifier, int value) {
        PlotFlag flag = getFlag(identifier);
        if (flag == null) return false;
        return Integer.parseInt(flag.getValue()) == value;
    }

    public boolean hasFlagValue(String identifier, double value) {
        PlotFlag flag = getFlag(identifier);
        if (flag == null) return false;
        return Double.parseDouble(flag.getValue()) == value;
    }

    public boolean hasFlagValue(String identifier, boolean value) {
        PlotFlag flag = getFlag(identifier);
        if (flag == null) return false;
        return Boolean.parseBoolean(flag.getValue()) == value;
    }

    public boolean hasFlagValue(String identifier, float value) {
        PlotFlag flag = getFlag(identifier);
        if (flag == null) return false;
        return Float.parseFloat(flag.getValue()) == value;
    }

    public boolean hasFlagValue(String identifier, long value) {
        PlotFlag flag = getFlag(identifier);
        if (flag == null) return false;
        return Long.parseLong(flag.getValue()) == value;
    }

    public boolean hasFlagValue(String identifier, Object value) {
        PlotFlag flag = getFlag(identifier);
        if (flag == null) return false;
        return flag.getValue().equals(value.toString());
    }

    public String getFlagsString() {
        StringBuilder builder = new StringBuilder("[");
        flags.forEach(f -> builder.append(f.getIdentifier()).append("=").append(f.getValue()).append(", "));
        if (builder.length() > 1) builder.delete(builder.length() - 2, builder.length());
        builder.append("]");

        return builder.toString();
    }

    public String getFlagsAsPrettyString() {
        StringBuilder builder = new StringBuilder();

        getFlags().forEach(flag -> builder.append("&b").append(flag.getIdentifier()).append(" &f= &d").append(flag.getValue()).append("&8, "));
        if (builder.toString().endsWith("&8, "))
            builder.delete(builder.length() - 4, builder.length() - 1);

        return builder.toString();
    }

    @Override
    public int compareTo(@NotNull PlotRole o) {
        return identifier.compareTo(o.getIdentifier());
    }
}
