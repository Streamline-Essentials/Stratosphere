package tv.quaint.stratosphere.plot.members.basic;

import tv.quaint.stratosphere.plot.SkyblockPlot;
import tv.quaint.stratosphere.plot.flag.PlotFlag;
import tv.quaint.stratosphere.plot.members.PlotRole;
import tv.quaint.stratosphere.plot.pos.PlotFlagIdentifiers;

public class MemberRole extends PlotRole {
    public MemberRole(SkyblockPlot plot) {
        super("member", "Member", plot);

        addFlag(new PlotFlag(PlotFlagIdentifiers.CAN_BREAK.getIdentifier(), true));
        addFlag(new PlotFlag(PlotFlagIdentifiers.CAN_PLACE.getIdentifier(), true));
        addFlag(new PlotFlag(PlotFlagIdentifiers.CAN_INTERACT.getIdentifier(), true));
        addFlag(new PlotFlag(PlotFlagIdentifiers.CAN_PLAYER_DAMAGE_GIVE.getIdentifier(), true));
        addFlag(new PlotFlag(PlotFlagIdentifiers.CAN_PLAYER_DAMAGE_TAKE.getIdentifier(), true));
        addFlag(new PlotFlag(PlotFlagIdentifiers.CAN_MOB_DAMAGE.getIdentifier(), true));
        addFlag(new PlotFlag(PlotFlagIdentifiers.CAN_MOB_SPAWN.getIdentifier(), true));
        addFlag(new PlotFlag(PlotFlagIdentifiers.CAN_MOB_TARGET.getIdentifier(), true));
        addFlag(new PlotFlag(PlotFlagIdentifiers.CAN_MOB_INTERACT.getIdentifier(), true));
        addFlag(new PlotFlag(PlotFlagIdentifiers.CAN_MOB_RIDE.getIdentifier(), true));
        addFlag(new PlotFlag(PlotFlagIdentifiers.CAN_MOB_BREED.getIdentifier(), true));
        addFlag(new PlotFlag(PlotFlagIdentifiers.CAN_MOB_TAME.getIdentifier(), true));
        addFlag(new PlotFlag(PlotFlagIdentifiers.CAN_MOB_SHEAR.getIdentifier(), true));
        addFlag(new PlotFlag(PlotFlagIdentifiers.CAN_FLY.getIdentifier(), true));
//        addFlag(new PlotFlag(PlotFlagIdentifiers.CAN_TRAMPLE.getIdentifier(), true));
        addFlag(new PlotFlag(PlotFlagIdentifiers.CAN_ANVIL_USE.getIdentifier(), true));
        addFlag(new PlotFlag(PlotFlagIdentifiers.CAN_BED_USE.getIdentifier(), true));
        addFlag(new PlotFlag(PlotFlagIdentifiers.CAN_BREWING_STAND_USE.getIdentifier(), true));
        addFlag(new PlotFlag(PlotFlagIdentifiers.CAN_CAMPFIRE_USE.getIdentifier(), true));
        addFlag(new PlotFlag(PlotFlagIdentifiers.CAN_CRAFTERS_USE.getIdentifier(), true));
        addFlag(new PlotFlag(PlotFlagIdentifiers.CAN_DOORS_USE.getIdentifier(), true));
        addFlag(new PlotFlag(PlotFlagIdentifiers.CAN_ENCHANTING_TABLE_USE.getIdentifier(), true));
        addFlag(new PlotFlag(PlotFlagIdentifiers.CAN_ENDER_CHEST_USE.getIdentifier(), true));
        addFlag(new PlotFlag(PlotFlagIdentifiers.CAN_FENCE_GATES_USE.getIdentifier(), true));
        addFlag(new PlotFlag(PlotFlagIdentifiers.CAN_FURNACES_USE.getIdentifier(), true));
        addFlag(new PlotFlag(PlotFlagIdentifiers.CAN_ITEM_FRAME_ROTATE.getIdentifier(), true));
        addFlag(new PlotFlag(PlotFlagIdentifiers.CAN_JUKEBOX_USE.getIdentifier(), true));
        addFlag(new PlotFlag(PlotFlagIdentifiers.CAN_CONTAINER_ACCESS.getIdentifier(), true));
        addFlag(new PlotFlag(PlotFlagIdentifiers.CAN_TRIGGER_REDSTONE.getIdentifier(), true));
        addFlag(new PlotFlag(PlotFlagIdentifiers.CAN_VEHICLE_PLACE.getIdentifier(), true));
        addFlag(new PlotFlag(PlotFlagIdentifiers.CAN_VILLAGER_TRADE.getIdentifier(), true));
//        addFlag(new PlotFlag(PlotFlagIdentifiers.CAN_VILLAGER_DAMAGE.getIdentifier(), false));

        addFlag(new PlotFlag(PlotFlagIdentifiers.CAN_SLIMEFUN_INTERACT.getIdentifier(), true));

        addFlag(new PlotFlag(PlotFlagIdentifiers.CAN_ITEM_FRAME_DESTROY.getIdentifier(), true));
        addFlag(new PlotFlag(PlotFlagIdentifiers.CAN_ARMOR_STAND_DESTROY.getIdentifier(), true));
        addFlag(new PlotFlag(PlotFlagIdentifiers.CAN_PAINTING_DESTROY.getIdentifier(), true));
        addFlag(new PlotFlag(PlotFlagIdentifiers.CAN_VEHICLE_DESTROY.getIdentifier(), true));

        addFlag(new PlotFlag(PlotFlagIdentifiers.PARENT.getIdentifier(), "visitor"));
    }
}
