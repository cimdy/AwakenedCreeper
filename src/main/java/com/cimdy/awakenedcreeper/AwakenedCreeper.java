package com.cimdy.awakenedcreeper;

import com.cimdy.awakenedcreeper.event.CreeperEvent;
import com.cimdy.awakenedcreeper.event.GenEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

@Mod(AwakenedCreeper.MODID)
public class AwakenedCreeper
{
    public static final String MODID = "awakened_creeper";

    public AwakenedCreeper(IEventBus modEventBus, ModContainer modContainer)
    {
        NeoForge.EVENT_BUS.addListener(CreeperEvent::onCreeperExplode);
        NeoForge.EVENT_BUS.addListener(CreeperEvent::onCreeperByHurt);
        NeoForge.EVENT_BUS.addListener(CreeperEvent::onCreeperByKill);
        NeoForge.EVENT_BUS.addListener(CreeperEvent::onCreeperPower);

        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SPEC);

        if (FMLEnvironment.dist.isClient()) {
            modEventBus.addListener(GenEvent::ServerGatherDataEvent);
        }

        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {}
}
