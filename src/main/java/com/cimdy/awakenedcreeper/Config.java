package com.cimdy.awakenedcreeper;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = AwakenedCreeper.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.IntValue DROP_CHANCE = BUILDER
            .comment("闪电苦力怕被击杀时掉落附魔书《不稳定导雷》")
            .defineInRange("概率", 100,0,100);

    private static final ModConfigSpec.BooleanValue THUNDER_POWER = BUILDER
            .comment("雷暴天气下，露天且未充能的苦力怕被闪电充能")
            .define("允许", true);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static int dropChance;
    public static Boolean thunderPower;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        dropChance = DROP_CHANCE.get();
        thunderPower = THUNDER_POWER.get();
    }
}