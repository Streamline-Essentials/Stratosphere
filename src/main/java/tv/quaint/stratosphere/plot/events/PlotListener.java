package tv.quaint.stratosphere.plot.events;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import tv.quaint.stratosphere.Stratosphere;
import tv.quaint.stratosphere.plot.PlotUtils;
import tv.quaint.stratosphere.plot.SkyblockPlot;
import tv.quaint.stratosphere.plot.members.PlotRole;
import tv.quaint.stratosphere.plot.pos.PlotFlagIdentifiers;

/**
 * This class will handle all plot interactions.
 * Plot interactions will be handled using Bukkit events.
 * Such as BlockBreakEvent, BlockPlaceEvent, PlayerInteractEvent, etc.
 *
 * This will make sure that players who do not have permission to edit a plot cannot do so.
 * It will use the PlotFlag class to determine what a player can and cannot do.
 */
public class PlotListener implements Listener {
    /**
     * Block break event.
     * This is a Player flag event.
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        SkyblockPlot plot = PlotUtils.getPlotByLocation(block.getLocation());
        if (plot == null) {
            Stratosphere.getInstance().logDebug("Plot is null");
            return;
        }

        PlotRole role = plot.getRole(player);
        if (role == null) {
            event.setCancelled(true);
            return;
        }

        boolean can = false;
        if (role.hasFlag(PlotFlagIdentifiers.CAN_BREAK.getIdentifier())) {
            can = Boolean.parseBoolean(role.getFlag(PlotFlagIdentifiers.CAN_BREAK.getIdentifier()).getValue());
        }
        if (! can) {
            event.setCancelled(true);
            return;
        }
    }

    /**
     * Block place event.
     * This is a Player flag event.
     */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        SkyblockPlot plot = PlotUtils.getPlotByLocation(block.getLocation());
        if (plot == null) return;

        PlotRole role = plot.getRole(player);
        if (role == null) {
            event.setCancelled(true);
            return;
        }

        boolean can = false;
        if (role.hasFlag(PlotFlagIdentifiers.CAN_PLACE.getIdentifier())) {
            can = Boolean.parseBoolean(role.getFlag(PlotFlagIdentifiers.CAN_PLACE.getIdentifier()).getValue());
        }
        if (! can) {
            event.setCancelled(true);
            return;
        }
    }

    /**
     * PVP Event
     * This is a Player flag event.
     */
    @EventHandler
    public void onPVPDamageOther(EntityDamageByEntityEvent event) {
        if (! (event.getEntity() instanceof Player)) return;
        if (! (event.getDamager() instanceof Player)) return;

        Player player = (Player) event.getDamager();

        SkyblockPlot plot = PlotUtils.getPlotByLocation(player.getLocation());
        if (plot == null) return;

        PlotRole role = plot.getRole(player);
        if (role == null) {
            event.setCancelled(true);
            return;
        }

        boolean can = false;
        if (role.hasFlag(PlotFlagIdentifiers.CAN_PLAYER_DAMAGE_GIVE.getIdentifier())) {
            can = Boolean.parseBoolean(role.getFlag(PlotFlagIdentifiers.CAN_PLAYER_DAMAGE_GIVE.getIdentifier()).getValue());
        }
        if (! can) {
            event.setCancelled(true);
            return;
        }
    }

    /**
     * PVP Event
     * This is a Player flag event.
     */
    @EventHandler
    public void onPVPDamageSelf(EntityDamageByEntityEvent event) {
        if (! (event.getEntity() instanceof Player)) return;
        if (! (event.getDamager() instanceof Player)) return;

        Player player = (Player) event.getEntity();

        SkyblockPlot plot = PlotUtils.getPlotByLocation(player.getLocation());
        if (plot == null) return;

        PlotRole role = plot.getRole(player);
        if (role == null) {
            event.setCancelled(true);
            return;
        }

        boolean can = false;
        if (role.hasFlag(PlotFlagIdentifiers.CAN_PLAYER_DAMAGE_TAKE.getIdentifier())) {
            can = Boolean.parseBoolean(role.getFlag(PlotFlagIdentifiers.CAN_PLAYER_DAMAGE_TAKE.getIdentifier()).getValue());
        }
        if (! can) {
            event.setCancelled(true);
            return;
        }
    }

    /**
     * Mob Damage Event
     * This is a Player flag event.
     */
    @EventHandler
    public void onMobDamage(EntityDamageByEntityEvent event) {
        if (! (event.getDamager() instanceof Player)) return;

        Player player = (Player) event.getDamager();

        SkyblockPlot plot = PlotUtils.getPlotByLocation(player.getLocation());
        if (plot == null) return;

        PlotRole role = plot.getRole(player);
        if (role == null) {
            event.setCancelled(true);
            return;
        }

        // check if it is a living entity
        if (event.getEntity() instanceof LivingEntity) {
            boolean can = false;
            if (role.hasFlag(PlotFlagIdentifiers.CAN_MOB_DAMAGE.getIdentifier())) {
                can = Boolean.parseBoolean(role.getFlag(PlotFlagIdentifiers.CAN_MOB_DAMAGE.getIdentifier()).getValue());
            }
            if (! can) {
                event.setCancelled(true);
                return;
            }

            // check if it is a villager
            if (event.getEntity() instanceof Villager) {
                boolean canVillager = false;
                if (role.hasFlag(PlotFlagIdentifiers.CAN_VILLAGER_DAMAGE.getIdentifier())) {
                    can = Boolean.parseBoolean(role.getFlag(PlotFlagIdentifiers.CAN_VILLAGER_DAMAGE.getIdentifier()).getValue());
                }
                if (! canVillager) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        // check if the mob is a pet
        if (event.getEntity() instanceof Tameable) {
            Tameable tameable = (Tameable) event.getEntity();
            if (tameable.getOwner() != player) {
                boolean can = false;
                if (role.hasFlag(PlotFlagIdentifiers.CAN_PET_DAMAGE.getIdentifier())) {
                    can = Boolean.parseBoolean(role.getFlag(PlotFlagIdentifiers.CAN_PET_DAMAGE.getIdentifier()).getValue());
                }
                if (! can) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        // check if it is a vehicle
        if (event.getEntity() instanceof Vehicle) {
            boolean can = false;
            if (role.hasFlag(PlotFlagIdentifiers.CAN_VEHICLE_DESTROY.getIdentifier())) {
                can = Boolean.parseBoolean(role.getFlag(PlotFlagIdentifiers.CAN_VEHICLE_DESTROY.getIdentifier()).getValue());
            }
            if (! can) {
                event.setCancelled(true);
                return;
            }
        }
    }

    /**
     * Can Mob Spawn Event
     * This is a World flag event.
     */
    @EventHandler
    public void onMobSpawn(EntitySpawnEvent event) {
        Entity entity = event.getEntity();

        SkyblockPlot plot = PlotUtils.getPlotByLocation(entity.getLocation());
        if (plot == null) return;

        boolean can = false;
        if (plot.hasFlag(PlotFlagIdentifiers.CAN_MOB_SPAWN.getIdentifier())) {
            can = Boolean.parseBoolean(plot.getFlag(PlotFlagIdentifiers.CAN_MOB_SPAWN.getIdentifier()).getValue());
        }
        if (! can) {
            event.setCancelled(true);
            return;
        }
    }

    /**
     * Can Mob Target Event
     * This is a Player flag event.
     */
    @EventHandler
    public void onMobTarget(EntityTargetEvent event) {
        if (! (event.getTarget() instanceof Player)) return;

        Player player = (Player) event.getTarget();

        SkyblockPlot plot = PlotUtils.getPlotByLocation(player.getLocation());
        if (plot == null) return;

        PlotRole role = plot.getRole(player);
        if (role == null) {
            event.setCancelled(true);
            return;
        }

        boolean can = false;
        if (role.hasFlag(PlotFlagIdentifiers.CAN_MOB_TARGET.getIdentifier())) {
            can = Boolean.parseBoolean(role.getFlag(PlotFlagIdentifiers.CAN_MOB_TARGET.getIdentifier()).getValue());
        }
        if (! can) {
            event.setCancelled(true);
            return;
        }
    }

    /**
     * Can Mob Pickup Event
     * This is a World flag event.
     */
    @EventHandler
    public void onMobPickup(EntityPickupItemEvent event) {
        Entity entity = event.getEntity();

        SkyblockPlot plot = PlotUtils.getPlotByLocation(entity.getLocation());
        if (plot == null) return;

        boolean can = false;
        if (plot.hasFlag(PlotFlagIdentifiers.CAN_MOB_PICKUP.getIdentifier())) {
            can = Boolean.parseBoolean(plot.getFlag(PlotFlagIdentifiers.CAN_MOB_PICKUP.getIdentifier()).getValue());
        }
        if (! can) {
            event.setCancelled(true);
            return;
        }
    }

    /**
     * Can Mob Drop Event
     * This is a World flag event.
     */
    @EventHandler
    public void onMobDrop(EntityDropItemEvent event) {
        Entity entity = event.getEntity();

        SkyblockPlot plot = PlotUtils.getPlotByLocation(entity.getLocation());
        if (plot == null) return;

        boolean can = false;
        if (plot.hasFlag(PlotFlagIdentifiers.CAN_MOB_DROP.getIdentifier())) {
            can = Boolean.parseBoolean(plot.getFlag(PlotFlagIdentifiers.CAN_MOB_DROP.getIdentifier()).getValue());
        }
        if (! can) {
            event.setCancelled(true);
            return;
        }
    }

    /**
     * Mob Interact Event
     * This is a Player flag event.
     */
    @EventHandler
    public void onMobInteract(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        SkyblockPlot plot = PlotUtils.getPlotByLocation(entity.getLocation());
        if (plot == null) return;

        PlotRole role = plot.getRole(player);
        if (role == null) {
            event.setCancelled(true);
            return;
        }

        boolean can = false;
        if (role.hasFlag(PlotFlagIdentifiers.CAN_MOB_INTERACT.getIdentifier())) {
            can = Boolean.parseBoolean(role.getFlag(PlotFlagIdentifiers.CAN_MOB_INTERACT.getIdentifier()).getValue());
        }
        if (! can) {
            event.setCancelled(true);
            return;
        }
    }

    /**
     * Can Mob Grief Event
     * This is a World flag event.
     */
    @EventHandler
    public void onMobGrief(EntityChangeBlockEvent event) {
        Entity entity = event.getEntity();

        SkyblockPlot plot = PlotUtils.getPlotByLocation(entity.getLocation());
        if (plot == null) return;

        boolean can = false;
        if (plot.hasFlag(PlotFlagIdentifiers.CAN_MOB_GRIEF.getIdentifier())) {
            can = Boolean.parseBoolean(plot.getFlag(PlotFlagIdentifiers.CAN_MOB_GRIEF.getIdentifier()).getValue());
        }
        if (! can) {
            event.setCancelled(true);
            return;
        }
    }

    /**
     * Mob Ride Event
     * This is a Player flag event.
     */
    @EventHandler
    public void onMobRide(VehicleEnterEvent event) {
        if (! (event.getEntered() instanceof Player)) return;

        Player player = (Player) event.getEntered();

        SkyblockPlot plot = PlotUtils.getPlotByLocation(event.getVehicle().getLocation());
        if (plot == null) return;

        PlotRole role = plot.getRole(player);
        if (role == null) {
            event.setCancelled(true);
            return;
        }

        boolean can = false;
        if (role.hasFlag(PlotFlagIdentifiers.CAN_MOB_RIDE.getIdentifier())) {
            can = Boolean.parseBoolean(role.getFlag(PlotFlagIdentifiers.CAN_MOB_RIDE.getIdentifier()).getValue());
        }
        if (! can) {
            event.setCancelled(true);
            return;
        }
    }

    /**
     * Mob Breed Event
     * This is a Player flag event.
     */
    @EventHandler
    public void onMobBreed(EntityBreedEvent event) {
        if (! (event.getBreeder() instanceof Player)) return;

        Player player = (Player) event.getBreeder();

        SkyblockPlot plot = PlotUtils.getPlotByLocation(event.getEntity().getLocation());
        if (plot == null) return;

        PlotRole role = plot.getRole(player);
        if (role == null) {
            event.setCancelled(true);
            return;
        }

        boolean can = false;
        if (role.hasFlag(PlotFlagIdentifiers.CAN_MOB_BREED.getIdentifier())) {
            can = Boolean.parseBoolean(role.getFlag(PlotFlagIdentifiers.CAN_MOB_BREED.getIdentifier()).getValue());
        }
        if (! can) {
            event.setCancelled(true);
            return;
        }
    }

    /**
     * Mob Tame Event
     * This is a Player flag event.
     */
    @EventHandler
    public void onMobTame(EntityTameEvent event) {
        if (! (event.getOwner() instanceof Player)) return;

        Player player = (Player) event.getOwner();

        SkyblockPlot plot = PlotUtils.getPlotByLocation(event.getEntity().getLocation());
        if (plot == null) return;

        PlotRole role = plot.getRole(player);
        if (role == null) {
            event.setCancelled(true);
            return;
        }

        boolean can = false;
        if (role.hasFlag(PlotFlagIdentifiers.CAN_MOB_TAME.getIdentifier())) {
            can = Boolean.parseBoolean(role.getFlag(PlotFlagIdentifiers.CAN_MOB_TAME.getIdentifier()).getValue());
        }
        if (! can) {
            event.setCancelled(true);
            return;
        }
    }

    /**
     * Mob Shear Event
     * This is a Player flag event.
     */
    @EventHandler
    public void onMobShear(PlayerShearEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getEntity();

        SkyblockPlot plot = PlotUtils.getPlotByLocation(entity.getLocation());
        if (plot == null) return;

        PlotRole role = plot.getRole(player);
        if (role == null) {
            event.setCancelled(true);
            return;
        }

        boolean can = false;
        if (role.hasFlag(PlotFlagIdentifiers.CAN_MOB_SHEAR.getIdentifier())) {
            can = Boolean.parseBoolean(role.getFlag(PlotFlagIdentifiers.CAN_MOB_SHEAR.getIdentifier()).getValue());
        }
        if (! can) {
            event.setCancelled(true);
            return;
        }
    }

    /**
     * Can Mob Explode Event
     * This is a World flag event.
     */
    @EventHandler
    public void onMobExplode(EntityExplodeEvent event) {
        Entity entity = event.getEntity();

        SkyblockPlot plot = PlotUtils.getPlotByLocation(entity.getLocation());
        if (plot == null) return;

        boolean can = false;
        if (plot.hasFlag(PlotFlagIdentifiers.CAN_MOB_EXPLODE.getIdentifier())) {
            can = Boolean.parseBoolean(plot.getFlag(PlotFlagIdentifiers.CAN_MOB_EXPLODE.getIdentifier()).getValue());
        }
        if (! can) {
            event.setCancelled(true);
            return;
        }
    }

    /**
     * Can Fly Event
     * This is a Player flag event.
     * This will be a movement event.
     * You will need to check if the player is flying, and
     * if they are, check if they have the flag -- if the don't
     * set them to not flying and teleport them to the plot spawn.
     * Also, if they are not flying, check if they have the flag
     * and if they do, set them to flying, but only if they are
     * already not flying.
     */
    @EventHandler
    public void onCanFly(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;

        SkyblockPlot plot = PlotUtils.getPlotByLocation(player.getLocation());
        if (plot == null) return;

        if (event.getTo() != null && event.getTo().getBlockY() <= 0.37d) {
            plot.getSpawnPos().teleport(player);
            return;
        }

        PlotRole role = plot.getRole(player);
        if (role == null) {
            player.setAllowFlight(false);
            return;
        }

        boolean can = false;
        if (role.hasFlag(PlotFlagIdentifiers.CAN_FLY.getIdentifier())) {
            can = Boolean.parseBoolean(role.getFlag(PlotFlagIdentifiers.CAN_FLY.getIdentifier()).getValue());
            if (! can) {
                player.setAllowFlight(false);
                if (player.isFlying()) {
                    player.setFlying(false);
                    plot.getSpawnPos().teleport(player);
                }
                return;
            } else {
                player.setAllowFlight(true);
                return;
            }
        }
    }

    /**
     * Can trample event
     * This is a player event.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCanTrample(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        SkyblockPlot plot = PlotUtils.getPlotByLocation(player.getLocation());
        if (plot == null) return;

        PlotRole role = plot.getRole(player);
        if (role == null) {
            event.setCancelled(true);
            return;
        }

        if (event.getAction() == Action.PHYSICAL) {
            if (event.getClickedBlock() == null) return;

            if (event.getClickedBlock().getType() == Material.FARMLAND) {
                boolean can = false;
                if (role.hasFlag(PlotFlagIdentifiers.CAN_TRAMPLE.getIdentifier())) {
                    can = Boolean.parseBoolean(role.getFlag(PlotFlagIdentifiers.CAN_TRAMPLE.getIdentifier()).getValue());
                }
                if (! can) {
                    event.setCancelled(true);
                    return;
                }
            }
            if (event.getClickedBlock().getType() == Material.STONE_PRESSURE_PLATE ||
                    event.getClickedBlock().getType() == Material.OAK_PRESSURE_PLATE ||
                    event.getClickedBlock().getType() == Material.SPRUCE_PRESSURE_PLATE ||
                    event.getClickedBlock().getType() == Material.BIRCH_PRESSURE_PLATE ||
                    event.getClickedBlock().getType() == Material.JUNGLE_PRESSURE_PLATE ||
                    event.getClickedBlock().getType() == Material.ACACIA_PRESSURE_PLATE ||
                    event.getClickedBlock().getType() == Material.DARK_OAK_PRESSURE_PLATE ||
                    event.getClickedBlock().getType() == Material.CRIMSON_PRESSURE_PLATE ||
                    event.getClickedBlock().getType() == Material.WARPED_PRESSURE_PLATE ||
                    event.getClickedBlock().getType() == Material.MANGROVE_PRESSURE_PLATE ||
                    event.getClickedBlock().getType() == Material.POLISHED_BLACKSTONE_PRESSURE_PLATE ||
                    event.getClickedBlock().getType() == Material.LIGHT_WEIGHTED_PRESSURE_PLATE ||
                    event.getClickedBlock().getType() == Material.HEAVY_WEIGHTED_PRESSURE_PLATE ||
                    event.getClickedBlock().getType() == Material.TRIPWIRE ||
                    event.getClickedBlock().getType() == Material.TRIPWIRE_HOOK) {
                boolean can = false;
                if (role.hasFlag(PlotFlagIdentifiers.CAN_TRIGGER_REDSTONE.getIdentifier())) {
                    can = Boolean.parseBoolean(role.getFlag(PlotFlagIdentifiers.CAN_TRIGGER_REDSTONE.getIdentifier()).getValue());
                }
                if (! can) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getClickedBlock() == null) return;

            if (event.getClickedBlock().getType() == Material.LEVER ||
                    event.getClickedBlock().getType() == Material.STONE_BUTTON ||
                    event.getClickedBlock().getType() == Material.OAK_BUTTON ||
                    event.getClickedBlock().getType() == Material.SPRUCE_BUTTON ||
                    event.getClickedBlock().getType() == Material.BIRCH_BUTTON ||
                    event.getClickedBlock().getType() == Material.JUNGLE_BUTTON ||
                    event.getClickedBlock().getType() == Material.ACACIA_BUTTON ||
                    event.getClickedBlock().getType() == Material.DARK_OAK_BUTTON ||
                    event.getClickedBlock().getType() == Material.CRIMSON_BUTTON ||
                    event.getClickedBlock().getType() == Material.WARPED_BUTTON ||
                    event.getClickedBlock().getType() == Material.MANGROVE_BUTTON ||
                    event.getClickedBlock().getType() == Material.POLISHED_BLACKSTONE_BUTTON ||
                    event.getClickedBlock().getType() == Material.REPEATER ||
                    event.getClickedBlock().getType() == Material.COMPARATOR ||
                    event.getClickedBlock().getType() == Material.DAYLIGHT_DETECTOR ||
                    event.getClickedBlock().getType() == Material.REDSTONE_TORCH ||
                    event.getClickedBlock().getType() == Material.REDSTONE_WALL_TORCH ||
                    event.getClickedBlock().getType() == Material.REDSTONE_LAMP ||
                    event.getClickedBlock().getType() == Material.REDSTONE_WIRE ||
                    event.getClickedBlock().getType() == Material.OBSERVER
            ) {
                if (role.getIdentifier().equals("owner")) return;

                boolean can = false;
                if (role.hasFlag(PlotFlagIdentifiers.CAN_TRIGGER_REDSTONE.getIdentifier())) {
                    can = Boolean.parseBoolean(role.getFlag(PlotFlagIdentifiers.CAN_TRIGGER_REDSTONE.getIdentifier()).getValue());
                }
                if (! can) {
                    event.setCancelled(true);
                    return;
                }
            }
            if (event.getClickedBlock().getType() == Material.CHEST ||
                    event.getClickedBlock().getType() == Material.TRAPPED_CHEST ||
                    event.getClickedBlock().getType() == Material.BARREL ||
                    event.getClickedBlock().getType() == Material.DROPPER ||
                    event.getClickedBlock().getType() == Material.DISPENSER ||
                    event.getClickedBlock().getType() == Material.HOPPER ||
                    event.getClickedBlock().getType() == Material.SHULKER_BOX ||
                    event.getClickedBlock().getType() == Material.BLACK_SHULKER_BOX ||
                    event.getClickedBlock().getType() == Material.BLUE_SHULKER_BOX ||
                    event.getClickedBlock().getType() == Material.BROWN_SHULKER_BOX ||
                    event.getClickedBlock().getType() == Material.CYAN_SHULKER_BOX ||
                    event.getClickedBlock().getType() == Material.GRAY_SHULKER_BOX ||
                    event.getClickedBlock().getType() == Material.GREEN_SHULKER_BOX ||
                    event.getClickedBlock().getType() == Material.LIGHT_BLUE_SHULKER_BOX ||
                    event.getClickedBlock().getType() == Material.LIGHT_GRAY_SHULKER_BOX ||
                    event.getClickedBlock().getType() == Material.LIME_SHULKER_BOX ||
                    event.getClickedBlock().getType() == Material.MAGENTA_SHULKER_BOX ||
                    event.getClickedBlock().getType() == Material.ORANGE_SHULKER_BOX ||
                    event.getClickedBlock().getType() == Material.PINK_SHULKER_BOX ||
                    event.getClickedBlock().getType() == Material.PURPLE_SHULKER_BOX ||
                    event.getClickedBlock().getType() == Material.RED_SHULKER_BOX ||
                    event.getClickedBlock().getType() == Material.WHITE_SHULKER_BOX ||
                    event.getClickedBlock().getType() == Material.YELLOW_SHULKER_BOX) {
                if (role.getIdentifier().equals("owner")) return;

                boolean can = false;
                if (role.hasFlag(PlotFlagIdentifiers.CAN_CONTAINER_ACCESS.getIdentifier())) {
                    can = Boolean.parseBoolean(role.getFlag(PlotFlagIdentifiers.CAN_CONTAINER_ACCESS.getIdentifier()).getValue());
                }
                if (! can) {
                    event.setCancelled(true);
                    return;
                }
            }
            if (event.getClickedBlock().getType() == Material.ANVIL ||
                    event.getClickedBlock().getType() == Material.CHIPPED_ANVIL ||
                    event.getClickedBlock().getType() == Material.DAMAGED_ANVIL
            ) {
                if (role.getIdentifier().equals("owner")) return;

                boolean can = false;
                if (role.hasFlag(PlotFlagIdentifiers.CAN_ANVIL_USE.getIdentifier())) {
                    can = Boolean.parseBoolean(role.getFlag(PlotFlagIdentifiers.CAN_ANVIL_USE.getIdentifier()).getValue());
                }
                if (! can) {
                    event.setCancelled(true);
                    return;
                }
            }
            if (event.getClickedBlock().getType() == Material.BEACON) {
                if (role.getIdentifier().equals("owner")) return;

                boolean can = false;
                if (role.hasFlag(PlotFlagIdentifiers.CAN_BEACON_USE.getIdentifier())) {
                    can = Boolean.parseBoolean(role.getFlag(PlotFlagIdentifiers.CAN_BEACON_USE.getIdentifier()).getValue());
                }
                if (! can) {
                    event.setCancelled(true);
                    return;
                }
            }
            if (event.getClickedBlock().getType() == Material.BLACK_BED ||
                    event.getClickedBlock().getType() == Material.BLUE_BED ||
                    event.getClickedBlock().getType() == Material.BROWN_BED ||
                    event.getClickedBlock().getType() == Material.CYAN_BED ||
                    event.getClickedBlock().getType() == Material.GRAY_BED ||
                    event.getClickedBlock().getType() == Material.GREEN_BED ||
                    event.getClickedBlock().getType() == Material.LIGHT_BLUE_BED ||
                    event.getClickedBlock().getType() == Material.LIGHT_GRAY_BED ||
                    event.getClickedBlock().getType() == Material.LIME_BED ||
                    event.getClickedBlock().getType() == Material.MAGENTA_BED ||
                    event.getClickedBlock().getType() == Material.ORANGE_BED ||
                    event.getClickedBlock().getType() == Material.PINK_BED ||
                    event.getClickedBlock().getType() == Material.PURPLE_BED ||
                    event.getClickedBlock().getType() == Material.RED_BED ||
                    event.getClickedBlock().getType() == Material.WHITE_BED ||
                    event.getClickedBlock().getType() == Material.YELLOW_BED
            ) {
                if (role.getIdentifier().equals("owner")) return;

                boolean can = false;
                if (role.hasFlag(PlotFlagIdentifiers.CAN_BED_USE.getIdentifier())) {
                    can = Boolean.parseBoolean(role.getFlag(PlotFlagIdentifiers.CAN_BED_USE.getIdentifier()).getValue());
                }
                if (! can) {
                    event.setCancelled(true);
                    return;
                }
            }
            if (event.getClickedBlock().getType() == Material.BREWING_STAND) {
                if (role.getIdentifier().equals("owner")) return;

                boolean can = false;
                if (role.hasFlag(PlotFlagIdentifiers.CAN_BREWING_STAND_USE.getIdentifier())) {
                    can = Boolean.parseBoolean(role.getFlag(PlotFlagIdentifiers.CAN_BREWING_STAND_USE.getIdentifier()).getValue());
                }
                if (! can) {
                    event.setCancelled(true);
                    return;
                }
            }
            if (event.getClickedBlock().getType() == Material.CRAFTING_TABLE ||
                    event.getClickedBlock().getType() == Material.CARTOGRAPHY_TABLE ||
                    event.getClickedBlock().getType() == Material.FLETCHING_TABLE ||
                    event.getClickedBlock().getType() == Material.GRINDSTONE ||
                    event.getClickedBlock().getType() == Material.LOOM ||
                    event.getClickedBlock().getType() == Material.SMITHING_TABLE ||
                    event.getClickedBlock().getType() == Material.STONECUTTER
            ) {
                if (role.getIdentifier().equals("owner")) return;

                boolean can = false;
                if (role.hasFlag(PlotFlagIdentifiers.CAN_CRAFTERS_USE.getIdentifier())) {
                    can = Boolean.parseBoolean(role.getFlag(PlotFlagIdentifiers.CAN_CRAFTERS_USE.getIdentifier()).getValue());
                }
                if (! can) {
                    event.setCancelled(true);
                    return;
                }
            }
            if (event.getClickedBlock().getType() == Material.IRON_DOOR ||
                    event.getClickedBlock().getType() == Material.ACACIA_DOOR ||
                    event.getClickedBlock().getType() == Material.BIRCH_DOOR ||
                    event.getClickedBlock().getType() == Material.DARK_OAK_DOOR ||
                    event.getClickedBlock().getType() == Material.JUNGLE_DOOR ||
                    event.getClickedBlock().getType() == Material.OAK_DOOR ||
                    event.getClickedBlock().getType() == Material.SPRUCE_DOOR ||
                    event.getClickedBlock().getType() == Material.IRON_TRAPDOOR ||
                    event.getClickedBlock().getType() == Material.ACACIA_TRAPDOOR ||
                    event.getClickedBlock().getType() == Material.BIRCH_TRAPDOOR ||
                    event.getClickedBlock().getType() == Material.DARK_OAK_TRAPDOOR ||
                    event.getClickedBlock().getType() == Material.JUNGLE_TRAPDOOR ||
                    event.getClickedBlock().getType() == Material.OAK_TRAPDOOR ||
                    event.getClickedBlock().getType() == Material.SPRUCE_TRAPDOOR ||
                    event.getClickedBlock().getType() == Material.MANGROVE_DOOR ||
                    event.getClickedBlock().getType() == Material.MANGROVE_TRAPDOOR ||
                    event.getClickedBlock().getType() == Material.WARPED_DOOR ||
                    event.getClickedBlock().getType() == Material.WARPED_TRAPDOOR ||
                    event.getClickedBlock().getType() == Material.CRIMSON_DOOR ||
                    event.getClickedBlock().getType() == Material.CRIMSON_TRAPDOOR
            ) {
                if (role.getIdentifier().equals("owner")) return;

                boolean can = false;
                if (role.hasFlag(PlotFlagIdentifiers.CAN_DOORS_USE.getIdentifier())) {
                    can = Boolean.parseBoolean(role.getFlag(PlotFlagIdentifiers.CAN_DOORS_USE.getIdentifier()).getValue());
                }
                if (! can) {
                    event.setCancelled(true);
                    return;
                }
            }
            if (event.getClickedBlock().getType() == Material.ENCHANTING_TABLE) {
                if (role.getIdentifier().equals("owner")) return;

                boolean can = false;
                if (role.hasFlag(PlotFlagIdentifiers.CAN_ENCHANTING_TABLE_USE.getIdentifier())) {
                    can = Boolean.parseBoolean(role.getFlag(PlotFlagIdentifiers.CAN_ENCHANTING_TABLE_USE.getIdentifier()).getValue());
                }
                if (! can) {
                    event.setCancelled(true);
                    return;
                }
            }
            if (event.getClickedBlock().getType() == Material.ENDER_CHEST) {
                if (role.getIdentifier().equals("owner")) return;

                boolean can = false;
                if (role.hasFlag(PlotFlagIdentifiers.CAN_ENDER_CHEST_USE.getIdentifier())) {
                    can = Boolean.parseBoolean(role.getFlag(PlotFlagIdentifiers.CAN_ENDER_CHEST_USE.getIdentifier()).getValue());
                }
                if (! can) {
                    event.setCancelled(true);
                    return;
                }
            }
            if (event.getClickedBlock().getType() == Material.OAK_FENCE_GATE ||
                    event.getClickedBlock().getType() == Material.ACACIA_FENCE_GATE ||
                    event.getClickedBlock().getType() == Material.BIRCH_FENCE_GATE ||
                    event.getClickedBlock().getType() == Material.DARK_OAK_FENCE_GATE ||
                    event.getClickedBlock().getType() == Material.JUNGLE_FENCE_GATE ||
                    event.getClickedBlock().getType() == Material.SPRUCE_FENCE_GATE ||
                    event.getClickedBlock().getType() == Material.MANGROVE_FENCE_GATE ||
                    event.getClickedBlock().getType() == Material.WARPED_FENCE_GATE ||
                    event.getClickedBlock().getType() == Material.CRIMSON_FENCE_GATE
            ) {
                if (role.getIdentifier().equals("owner")) return;

                boolean can = false;
                if (role.hasFlag(PlotFlagIdentifiers.CAN_FENCE_GATES_USE.getIdentifier())) {
                    can = Boolean.parseBoolean(role.getFlag(PlotFlagIdentifiers.CAN_FENCE_GATES_USE.getIdentifier()).getValue());
                }
                if (! can) {
                    event.setCancelled(true);
                    return;
                }
            }
            if (event.getClickedBlock().getType() == Material.FURNACE ||
                    event.getClickedBlock().getType() == Material.BLAST_FURNACE ||
                    event.getClickedBlock().getType() == Material.SMOKER
            ) {
                if (role.getIdentifier().equals("owner")) return;

                boolean can = false;
                if (role.hasFlag(PlotFlagIdentifiers.CAN_FURNACES_USE.getIdentifier())) {
                    can = Boolean.parseBoolean(role.getFlag(PlotFlagIdentifiers.CAN_FURNACES_USE.getIdentifier()).getValue());
                }
                if (! can) {
                    event.setCancelled(true);
                    return;
                }
            }
            if (event.getClickedBlock().getType() == Material.JUKEBOX) {
                if (role.getIdentifier().equals("owner")) return;

                boolean can = false;
                if (role.hasFlag(PlotFlagIdentifiers.CAN_JUKEBOX_USE.getIdentifier())) {
                    can = Boolean.parseBoolean(role.getFlag(PlotFlagIdentifiers.CAN_JUKEBOX_USE.getIdentifier()).getValue());
                }
                if (! can) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
        // Item frame
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getClickedBlock() == null) return;

            if (event.getClickedBlock().getType() == Material.ITEM_FRAME) {
                if (role.getIdentifier().equals("owner")) return;

                boolean can = false;
                if (role.hasFlag(PlotFlagIdentifiers.CAN_ITEM_FRAME_ROTATE.getIdentifier())) {
                    can = Boolean.parseBoolean(role.getFlag(PlotFlagIdentifiers.CAN_ITEM_FRAME_ROTATE.getIdentifier()).getValue());
                }
                if (! can) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
        // Destroy item frame, armor stand, painting
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (event.getClickedBlock() == null) return;
            if (event.getClickedBlock().getType() == Material.ITEM_FRAME) {
                if (role.getIdentifier().equals("owner")) return;

                boolean can = false;
                if (role.hasFlag(PlotFlagIdentifiers.CAN_ITEM_FRAME_DESTROY.getIdentifier())) {
                    can = Boolean.parseBoolean(role.getFlag(PlotFlagIdentifiers.CAN_ITEM_FRAME_DESTROY.getIdentifier()).getValue());
                }
                if (! can) {
                    event.setCancelled(true);
                    return;
                }
            }
            if (event.getClickedBlock().getType() == Material.ARMOR_STAND) {
                if (role.getIdentifier().equals("owner")) return;

                boolean can = false;
                if (role.hasFlag(PlotFlagIdentifiers.CAN_ARMOR_STAND_DESTROY.getIdentifier())) {
                    can = Boolean.parseBoolean(role.getFlag(PlotFlagIdentifiers.CAN_ARMOR_STAND_DESTROY.getIdentifier()).getValue());
                }
                if (! can) {
                    event.setCancelled(true);
                    return;
                }
            }
            if (event.getClickedBlock().getType() == Material.PAINTING) {
                if (role.getIdentifier().equals("owner")) return;

                boolean can = false;
                if (role.hasFlag(PlotFlagIdentifiers.CAN_PAINTING_DESTROY.getIdentifier())) {
                    can = Boolean.parseBoolean(role.getFlag(PlotFlagIdentifiers.CAN_PAINTING_DESTROY.getIdentifier()).getValue());
                }
                if (! can) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    /**
     * Arrow hit painting, armorstand, or item frame event.
     * This is a player event.
     */
    @EventHandler
    public void onArrowHitDestroyable(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getDamager();
            if (arrow.getShooter() instanceof Player) {
                Player player = (Player) arrow.getShooter();

                SkyblockPlot plot = PlotUtils.getPlotByLocation(player.getLocation());
                if (plot == null) return;

                // Checking for role needs to be outside of if statements that are below (inside this nest) | Do not copy this comment.
                PlotRole role = plot.getRole(player);
                if (role == null) {
                    event.setCancelled(true);
                    return;
                }

                if (event.getEntity() instanceof ItemFrame) {
                    boolean can = false;
                    if (role.hasFlag(PlotFlagIdentifiers.CAN_ITEM_FRAME_DESTROY.getIdentifier())) {
                        can = Boolean.parseBoolean(role.getFlag(PlotFlagIdentifiers.CAN_ITEM_FRAME_DESTROY.getIdentifier()).getValue());
                    }
                    if (! can) {
                        event.setCancelled(true);
                        return;
                    }
                }
                if (event.getEntity() instanceof ArmorStand) {
                    boolean can = false;
                    if (role.hasFlag(PlotFlagIdentifiers.CAN_ARMOR_STAND_DESTROY.getIdentifier())) {
                        can = Boolean.parseBoolean(role.getFlag(PlotFlagIdentifiers.CAN_ARMOR_STAND_DESTROY.getIdentifier()).getValue());
                    }
                    if (! can) {
                        event.setCancelled(true);
                        return;
                    }
                }
                if (event.getEntity() instanceof Painting) {
                    boolean can = false;
                    if (role.hasFlag(PlotFlagIdentifiers.CAN_PAINTING_DESTROY.getIdentifier())) {
                        can = Boolean.parseBoolean(role.getFlag(PlotFlagIdentifiers.CAN_PAINTING_DESTROY.getIdentifier()).getValue());
                    }
                    if (! can) {
                        event.setCancelled(true);
                        return;
                    }
                }

                // Also check if it hits another player.
                if (event.getEntity() instanceof Player) {
                    boolean can = false;
                    if (role.hasFlag(PlotFlagIdentifiers.CAN_PLAYER_DAMAGE_GIVE.getIdentifier())) {
                        can = Boolean.parseBoolean(role.getFlag(PlotFlagIdentifiers.CAN_PLAYER_DAMAGE_GIVE.getIdentifier()).getValue());
                    }
                    if (! can) {
                        event.setCancelled(true);
                        return;
                    }

                    Player hitPlayer = (Player) event.getEntity();
                    PlotRole hitRole = plot.getRole(hitPlayer);

                    if (hitRole == null) {
                        event.setCancelled(true);
                        return;
                    }

                    boolean canHit = false;
                    if (hitRole.hasFlag(PlotFlagIdentifiers.CAN_PLAYER_DAMAGE_TAKE.getIdentifier())) {
                        canHit = Boolean.parseBoolean(hitRole.getFlag(PlotFlagIdentifiers.CAN_PLAYER_DAMAGE_TAKE.getIdentifier()).getValue());
                    }
                    if (! canHit) {
                        event.setCancelled(true);
                        return;
                    }
                }

                // Check if it hits a mob.
                if (event.getEntity() instanceof LivingEntity) {
                    boolean can = false;
                    if (role.hasFlag(PlotFlagIdentifiers.CAN_MOB_DAMAGE.getIdentifier())) {
                        can = Boolean.parseBoolean(role.getFlag(PlotFlagIdentifiers.CAN_MOB_DAMAGE.getIdentifier()).getValue());
                    }
                    if (! can) {
                        event.setCancelled(true);
                        return;
                    }
                }

                // Check if it hits a vehicle.
                if (event.getEntity() instanceof Vehicle) {
                    boolean can = false;
                    if (role.hasFlag(PlotFlagIdentifiers.CAN_VEHICLE_DESTROY.getIdentifier())) {
                        can = Boolean.parseBoolean(role.getFlag(PlotFlagIdentifiers.CAN_VEHICLE_DESTROY.getIdentifier()).getValue());
                    }
                    if (! can) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }
}
