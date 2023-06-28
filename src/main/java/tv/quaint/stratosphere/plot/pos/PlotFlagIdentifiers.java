package tv.quaint.stratosphere.plot.pos;

import lombok.Getter;

public enum PlotFlagIdentifiers {
        CAN_BREAK("can_break"),
        CAN_PLACE("can_place"),
        CAN_INTERACT("can_interact"),
        CAN_PLAYER_DAMAGE_GIVE("can_player_damage_other"),
        CAN_PLAYER_DAMAGE_TAKE("can_player_damage_self"),
        CAN_MOB_DAMAGE("can_mob_damage"),
        CAN_PET_DAMAGE("can_pet_damage"),
        CAN_MOB_SPAWN("can_mob_spawn", PlotFlagType.WORLD),
        CAN_MOB_TARGET("can_mob_target"),
        CAN_MOB_PICKUP("can_mob_pickup", PlotFlagType.WORLD),
        CAN_MOB_DROP("can_mob_drop", PlotFlagType.WORLD),
        CAN_MOB_INTERACT("can_mob_interact"),
        CAN_MOB_GRIEF("can_mob_grief", PlotFlagType.WORLD),
        CAN_MOB_RIDE("can_mob_ride"),
        CAN_MOB_BREED("can_mob_breed"),
        CAN_MOB_TAME("can_mob_tame"),
        CAN_MOB_SHEAR("can_mob_shear"),
        CAN_MOB_EXPLODE("can_mob_explode", PlotFlagType.WORLD),
        CAN_FLY("can_fly"),
        CAN_TRAMPLE("can_trample"),
        CAN_ANVIL_USE("can_anvil_use"),
        CAN_BEACON_USE("can_beacon_use"),
        CAN_BED_USE("can_bed_use"),
        CAN_BREWING_STAND_USE("can_brewing_stand_use"),
//        CAN_BUTTON_USE("can_button_use"),
//        CAN_CHEST_ACCESS("can_chest_access"),
        CAN_CAMPFIRE_USE("can_campfire_use"),
        CAN_CRAFTERS_USE("can_crafters_use"),
        CAN_DOORS_USE("can_doors_use"),
        CAN_ENCHANTING_TABLE_USE("can_enchanting_table_use"),
        CAN_ENDER_CHEST_USE("can_ender_chest_use"),
        CAN_FENCE_GATES_USE("can_fence_gates_use"),
        CAN_FURNACES_USE("can_furnaces_use"),
//        CAN_HOPPER_USE("can_hopper_use"),
        CAN_ITEM_FRAME_ROTATE("can_item_frame_rotate"),
        CAN_JUKEBOX_USE("can_jukebox_use"),
//        CAN_LEVER_USE("can_lever_use"),
        CAN_CONTAINER_ACCESS("can_container_access"),
        CAN_TRIGGER_REDSTONE("can_trigger_redstone"),
        CAN_VEHICLE_PLACE("can_vechile_use"),
        CAN_VILLAGER_TRADE("can_villager_trade"),
        CAN_VILLAGER_DAMAGE("can_villager_damage"),

        CAN_SLIMEFUN_INTERACT("can_slimefun_interact"),

        CAN_ITEM_FRAME_DESTROY("can_item_frame_destroy"),
        CAN_ARMOR_STAND_DESTROY("can_armor_stand_destroy"),
        CAN_PAINTING_DESTROY("can_painting_destroy"),
        CAN_VEHICLE_DESTROY("can_vehicle_destroy"),

        ADMIN("admin"),
        TRUST_USERS("trust_users"),
        PERMISSION_EDIT("permission_edit"),
        
        PARENT("parent", PlotFlagType.ROLE),
        ;
        
        @Getter
        private final String identifier;
        @Getter
        private final PlotFlagType type;
        
        PlotFlagIdentifiers(String identifier, PlotFlagType type) {
            this.identifier = identifier;
            this.type = type;
        }
        
        PlotFlagIdentifiers(String identifier) {
            this(identifier, PlotFlagType.PLAYER);
        }
    }