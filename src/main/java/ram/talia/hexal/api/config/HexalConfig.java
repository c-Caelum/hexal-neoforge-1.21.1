package ram.talia.hexal.api.config;

import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

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
                .comment("The maximum amount of records allowed in a each Mote Nexus.")
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

        public static final ModConfigSpec.DoubleValue SUMMON_TICKING_WISP_COST = BUILDER
                .comment("")
                .comment("The cost of summoning a cyclic wisp.")
                .defineInRange("summonCyclicWispCost", 3.0, 0.0, 100000.0);

        public static int getMaxItemsReturned() {
            return MAX_ITEMS_RETURNED.get();
        }
        public static int getMaxRecordsInMediafiedStorage() {
            return MAX_RECORDS_IN_MEDIAFIED_STORAGE.get();
        }

        public static final ModConfigSpec SPEC = BUILDER.build();
    }
}
