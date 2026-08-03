package ram.talia.hexal.api.config;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
public class HexalConfig {


    public static class Server {
        private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

        public static final ModConfigSpec.IntValue MAX_ITEMS_RETURNED = BUILDER
                .comment("The maximum amount of items that a given mote return use can return.")
                .comment("Capped to be a positive integer between 640 and 64000.")
                .defineInRange("maxItemsReturned", 32000, 64, 64000);
        public static final ModConfigSpec.IntValue MAX_RECORDS_IN_MEDIAFIED_STORAGE = BUILDER
                .comment("The maximum amount of records allowed in each Mote Nexus.")
                .comment("Capped to be a positive integer between 128 and 16384.")
                .defineInRange("maxRecordsForNexus", 1023, 128, 16384);
        public static final ModConfigSpec.DoubleValue BIND_STORAGE_COST = BUILDER
                .comment("The cost of setting your permanent mote nexus.")
                .defineInRange("bindMoteNexusCost", 32.0, 0.0, 100000.0);
        public static final ModConfigSpec.DoubleValue TEMP_BIND_STORAGE_COST = BUILDER
                .comment("The cost of *temporarily* setting your permanent mote nexus.")
                .defineInRange("tempBindMoteNexusCost", 0.001, 0.0, 100000.0);
        public static final ModConfigSpec.DoubleValue MEDIAFY_ITEM_COST = BUILDER
                .comment("The cost of mediafying an item into a mote.")
                .defineInRange("mediafyItemCost", 0.1, 0.0, 100000.0);
        public static final ModConfigSpec.DoubleValue RETURN_ITEM_COST = BUILDER
                .comment("The cost of returning an item from a mote.")
                .defineInRange("returnItemCost", 0.1, 0.0, 100000.0);
        public static final ModConfigSpec.DoubleValue CRAFT_ITEM_COST = BUILDER
                .comment("The cost of crafting an item from a mote.")
                .defineInRange("craftItemCost", 0.1, 0.0, 100000.0);
        public static final ModConfigSpec.DoubleValue TRADE_ITEM_COST = BUILDER
                .comment("The cost of trading with a villager from a mote.")
                .defineInRange("tradeItemCost", 0.1, 0.0, 100000.0);
        public static final ModConfigSpec.DoubleValue USE_ITEM_ON_COST = BUILDER
                .comment("The cost of using a mote on a block.")
                .defineInRange("useItemOnCost", 0.25, 0.0, 100000.0);

        /*
                    const val DEFAULT_PROJECTILE_WISP_UPKEEP_PER_TICK =  0.325 / 20.0
            const val DEFAULT_UNTRIGGERED_WISP_UPKEEP_DISCOUNT = 0.77
            const val DEFAULT_SEON_DISCOUNT_FACTOR = 20.0
            const val DEFAULT_STORING_PLAYER_COST_SCALE_FACTOR = 20.0
            const val DEFAULT_MEDIA_FLOW_RATE_OVER_LINK = 0.01
         */

        public static final ModConfigSpec.DoubleValue TICKING_WISP_UPKEEP = BUILDER
                .comment("The cost of upkeeping a cylic wisp, in dust per tick.")
                .defineInRange("cyclicWispUpkeep", 0.65 / 20.0, 0.0, 100000.0);
        public static final ModConfigSpec.DoubleValue PROJECTILE_WISP_UPKEEP = BUILDER
                .comment("The cost of upkeeping a projectile wisp, in dust per tick.")
                .defineInRange("projectileWispUpkeep", 0.325 / 20.0, 0.0, 100000.0);
        public static final ModConfigSpec.DoubleValue PAUSED_WISP_DISCOUNT = BUILDER
                .comment("The discount of a paused wisp.")
                .defineInRange("pausedWispUpkeep", 0.77, 0.0, 100000.0);
        public static final ModConfigSpec.DoubleValue BOUND_WISP_DISCOUNT_FACTOR = BUILDER
                .comment("The discount factor (the cost is divided by this) for a bound wisp.")
                .defineInRange("boundWispUpkeep", 20.0, 0.0, 100000.0);
        public static final ModConfigSpec.DoubleValue PLAYER_WISP_UPKEEP = BUILDER
                .comment("The cost multiplier for having a player entity in a wisp somewhere.")
                .comment("More specifically, cost is calculated as this^numPlayers.")
                .defineInRange("playerContainingWispUpkeep", 20.0, 0.0, 100000.0);

        public static final ModConfigSpec.DoubleValue MOVE_SPEED_SET_COST = BUILDER
                .comment("")
                .comment("The cost of setting the move speed of a wisp. Not static, based on highest wisp speed set.")
                .defineInRange("setMoveSpeedCost",1.0, 0.0, 100000.0);

        public static final ModConfigSpec.DoubleValue SUMMON_TICKING_WISP_COST = BUILDER
                .comment("The cost of summoning a cyclic wisp.")
                .defineInRange("summonCyclicWispCost", 3.0, 0.0, 100000.0);

        public static final ModConfigSpec.DoubleValue SUMMON_PROJECTILE_WISP_COST = BUILDER
                .comment("The cost of summoning a cyclic wisp. Minimum is 0.5 dust.")
                .defineInRange("summonProjectileWispCost", 1.7, 0.5, 100000.0);

        public static final ModConfigSpec.DoubleValue FALLING_BLOCK_COST = BUILDER
                .comment("The cost for making a block fall.")
                .defineInRange("fallingBlockCost", 1.5, 0.0, 100000.0);

        public static final ModConfigSpec.DoubleValue FREEZE_COST = BUILDER
                .comment("The cost for freezing a block.")
                .defineInRange("freezeCost", 1, 0.0, 100000.0);

        public static final ModConfigSpec.DoubleValue PARTICLES_COST = BUILDER
                .comment("The cost for summoning a particle.")
                .defineInRange("particlesCost", 0.002, 0.0, 100000.0);

        public static final ModConfigSpec.DoubleValue PLACE_TYPE_COST = BUILDER
                .comment("The cost for summoning a particle.")
                .defineInRange("placeTypeCost", 0.125, 0.0, 100000.0);

        public static final ModConfigSpec.DoubleValue SMELT_COST = BUILDER
                .comment("The cost for smelting one item/mote/block.")
                .defineInRange("smeltCost", 0.75, 0.0, 100000.0);

        public static final ModConfigSpec.DoubleValue MAKE_GATE_COST = BUILDER
                .comment("The cost for making a gate.")
                .defineInRange("makeGateCost", 320.0, 0.0, 100000.0);
        public static final ModConfigSpec.DoubleValue MARK_GATE_COST = BUILDER
                .comment("The cost for marking an entity for a gate.")
                .defineInRange("markEntityCost", 0.05, 0.0, 100000.0);
        public static final ModConfigSpec.DoubleValue CLOSE_GATE_COST = BUILDER
                .comment("The cost for closing a gate.")
                .defineInRange("closeGateCost", 2.5, 0.0, 100000.0);
        public static final ModConfigSpec.DoubleValue CLOSE_GATE_DISTANCE_COST_SCALE_FACTOR = BUILDER
                .comment("The scale factor for a drifting gate's distance from the target.")
                .defineInRange("driftingScaleFactor", 0.1, 0.0, 100000.0);
        public static final ModConfigSpec.DoubleValue MAX_GATE_OFFSET = BUILDER
                .comment("The maximum offset that an entity-anchored gate can have.")
                .defineInRange("maxGateOffset", 32, 0.0, 100000.0);

        public static final ModConfigSpec.DoubleValue CONSUME_WISP_OWN_WISP = BUILDER
                .comment("The cost scaling factor for consuming *your own* wisp.")
                .defineInRange("consumeOwnWispCost", 5.0, 0.0, 100000.0);
        public static final ModConfigSpec.DoubleValue CONSUME_WISP_OTHERS_COST_PER_MEDIA = BUILDER
                .comment("The cost scaling factor for consuming someone else's wisp.")
                .defineInRange("consumeWispScalingFactor", 1.5, 0.0, 100000.0);
        public static final ModConfigSpec.DoubleValue SEON_WISP_SET_COST = BUILDER
                .comment("The cost for setting your bound wisp.")
                .defineInRange("setBoundWispCost", 50.0, 0.0, 100000.0);
        public static final ModConfigSpec.DoubleValue TICK_CONSTANT_COST = BUILDER
                .comment("The constant cost for ticking a block.")
                .defineInRange("tickConstantCost", 0.1, 0.0, 100000.0);
        public static final ModConfigSpec.DoubleValue TICK_COST_PER_TICKED = BUILDER
                .comment("The scaling factor for ticking a block.")
                .defineInRange("tickScalingFactor", 0.001, 0.0, 100000.0);

        public static final ModConfigSpec.IntValue TICK_RANDOM_TICK_I_PROB = BUILDER
                .comment("A random tick from Accelerate happens one in N times, where N is this number.")
                .defineInRange("randomTickProbability", 1365, 0, 2100);

        public static final ModConfigSpec.ConfigValue<List<? extends String>> DISALLOWED_BLOCKS_TICK = BUILDER
                .comment("Disallowed blocks in Accelerate.")
                .defineListAllowEmpty("accelerateDisallowed", List.of("hexcasting:impetus_look", "create:deployer"),
                        () -> "", Server::validateBlock);

        public static final ModConfigSpec.DoubleValue SLIPWAY_CHANCE = BUILDER
                .comment("The chance for a slipway to form is this number.")
                .comment("Set to 0 for none to spawn.")
                .defineInRange("slipwayChance", 0.5, 0, 1);


        public static boolean isAccelerateAllowed(ResourceLocation blockID) {
            return !(DISALLOWED_BLOCKS_TICK.get().contains(blockID.toString()));
        }

        public static int getMaxItemsReturned() {
            return MAX_ITEMS_RETURNED.get();
        }
        public static int getMaxRecordsInMediafiedStorage() {
            return MAX_RECORDS_IN_MEDIAFIED_STORAGE.get();
        }

        private static boolean validateBlock(Object object) {
            return object instanceof String itemName && ResourceLocation.tryParse(itemName) != null;
        }

        public static final ModConfigSpec SPEC = BUILDER.build();
    }

    public static class Client {
       public static final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

       public static final ModConfigSpec.DoubleValue PARTICLE_CHANCE = builder.
    }
}
