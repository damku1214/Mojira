package net.damku1214.mojira;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

@Mod(Mojira.MOD_ID)
public class Mojira {
    public static final String MOD_ID = "mojira";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Mojira(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.register(this);

        modContainer.registerConfig(ModConfig.Type.COMMON, MojiraConfig.CONFIG_SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Mojira is loaded - COMMON");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Mojira is loaded - SERVER");
    }
}
