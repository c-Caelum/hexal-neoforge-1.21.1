package ram.talia.hexal;

import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.common.lib.HexRegistries;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import com.mojang.logging.LogUtils;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.slf4j.Logger;
import ram.talia.hexal.api.config.HexalConfig;
import ram.talia.hexal.api.gates.GateManager;
import ram.talia.hexal.client.HexalClient;
import ram.talia.hexal.common.lib.*;
import ram.talia.hexal.common.lib.hex.HexalActions;
import ram.talia.hexal.common.lib.hex.HexalArithmetics;
import ram.talia.hexal.common.lib.hex.HexalIotaTypes;
import ram.talia.hexal.common.lib.recipe.FreezeRecipe;
import ram.talia.hexal.datagen.HexalActionTagProvider;
import ram.talia.hexal.datagen.HexalBlockTags;
import ram.talia.hexal.datagen.HexalplatRecipes;
import ram.talia.hexal.eventhandlers.BoundStorageEventHandler;
import ram.talia.hexal.eventhandlers.WispCastingManagerEventHandler;

import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Hexal.MODID)
public class Hexal {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "hexal";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final boolean isSable = IXplatAbstractions.INSTANCE.isModPresent("sable");

    public static final EntityDataSerializer<FrozenPigment> PIGMENT_SERIALIZER = EntityDataSerializer.forValueType(FrozenPigment.STREAM_CODEC);

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public Hexal(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::onDatagen);
        modEventBus.addListener(HexalClient::registerEntityRenderers);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (Hexal) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        bind(Registries.BLOCK, HexalBlocks::registerBlocks, modEventBus);
        bind(Registries.ITEM, HexalBlocks::registerBlockItems, modEventBus);
        bind(Registries.BLOCK_ENTITY_TYPE, HexalBlockEntities::registerBlockEntities, modEventBus);
        bind(HexRegistries.IOTA_TYPE, HexalIotaTypes::registerTypes, modEventBus);
        bind(HexRegistries.ACTION, HexalActions::register, modEventBus);
        bind(HexRegistries.ARITHMETIC, HexalArithmetics::register, modEventBus);
        bind(Registries.ENTITY_TYPE, HexalEntities::registerEntities, modEventBus);

        //Made inline not to create a separate class for an entire ONE recipe type.
        bind(Registries.RECIPE_SERIALIZER, consumer -> {
            consumer.accept(FreezeRecipe.SERIALIZER, FreezeRecipe.FREEZE_RECIPE_TYPE_LOCATION);
        }, modEventBus);
        bind(Registries.RECIPE_TYPE, consumer -> {
            consumer.accept(FreezeRecipe.TYPE, FreezeRecipe.FREEZE_RECIPE_TYPE_LOCATION);
        }, modEventBus);

        modEventBus.addListener((RegisterEvent event) -> {
            event.register(NeoForgeRegistries.ENTITY_DATA_SERIALIZERS.key(), registryHelper -> {
                registryHelper.register(modLoc("pigment"), PIGMENT_SERIALIZER);
            });
        });
        HexalPacketHandler.init(modEventBus);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.SERVER, HexalConfig.Server.SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, HexalConfig.Client.SPEC);
    }


    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");
    }

    public static Long toMediaFromDust(double dust) {
        return (long) (dust*((double)MediaConstants.DUST_UNIT));
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
    }

    public static ResourceLocation modLoc(String str) {
        return ResourceLocation.fromNamespaceAndPath(MODID, str);
    }

    public void onDatagen(GatherDataEvent event) {
        final DataGenerator gen = event.getGenerator();
        gen.addProvider(event.includeServer(), new HexalplatRecipes(gen.getPackOutput(), event.getLookupProvider()));
        gen.addProvider(event.includeServer(), new HexalActionTagProvider(gen.getPackOutput(), event.getLookupProvider()));
        gen.addProvider(event.includeServer(), new LootTableProvider(
                gen.getPackOutput(), Set.of(), List.of(new LootTableProvider.SubProviderEntry(HexalLootTables::new, LootContextParamSets.ALL_PARAMS)),
                event.getLookupProvider()
        ));
        gen.addProvider(event.includeServer(), new HexalBlockTags(gen.getPackOutput(), event.getLookupProvider()));
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
        GateManager.extraInit(event.getServer());
        NeoForge.EVENT_BUS.addListener(WispCastingManagerEventHandler::playerLoggedIn);
        NeoForge.EVENT_BUS.addListener(WispCastingManagerEventHandler::playerLoggedOut);
        NeoForge.EVENT_BUS.addListener(WispCastingManagerEventHandler::serverTick);
        NeoForge.EVENT_BUS.addListener(BoundStorageEventHandler::playerLoggedIn);
        NeoForge.EVENT_BUS.addListener(BoundStorageEventHandler::playerLoggedOut);
    }

    public static Iota deserializeIota(Tag tag) {
        return IotaType.TYPED_CODEC.decode(NbtOps.INSTANCE, tag).getOrThrow().getFirst();
    }

    public static Tag serializeIota(Iota iota) {
        return IotaType.TYPED_CODEC.encodeStart(NbtOps.INSTANCE, iota).getOrThrow();
    }

    private <T> void bind(ResourceKey<? extends Registry<T>> registry, Consumer<BiConsumer<T, ResourceLocation>> source, IEventBus bus) {
        bus.addListener((RegisterEvent event) -> {
            event.register(registry, actionRegistryEntryRegisterHelper -> {
                source.accept((t, rl) -> actionRegistryEntryRegisterHelper.register(rl, t));
            });
        });
    }

}
