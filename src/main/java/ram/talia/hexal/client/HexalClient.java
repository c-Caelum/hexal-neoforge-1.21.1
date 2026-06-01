package ram.talia.hexal.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import ram.talia.hexal.Hexal;
import ram.talia.hexal.client.blocks.BlockEntityMediafiedStorageRenderer;
import ram.talia.hexal.common.lib.HexalBlockEntities;
import vazkii.patchouli.neoforge.client.NeoForgeClientInitializer;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = Hexal.MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = Hexal.MODID, value = Dist.CLIENT)
public class HexalClient {
    public HexalClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                // The block entity type to register the renderer for.
                HexalBlockEntities.MEDIAFIED_STORAGE,
                // A function of BlockEntityRendererProvider.Context to BlockEntityRenderer.
                BlockEntityMediafiedStorageRenderer::new
        );
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
        Hexal.LOGGER.info("HELLO FROM CLIENT SETUP");
        Hexal.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }
}
