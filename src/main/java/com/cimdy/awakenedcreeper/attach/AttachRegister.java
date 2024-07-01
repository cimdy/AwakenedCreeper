package com.cimdy.awakenedcreeper.attach;

import com.cimdy.awakenedcreeper.AwakenedCreeper;
import com.mojang.serialization.Codec;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class AttachRegister {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, AwakenedCreeper.MODID);

    public static final Supplier<AttachmentType<Integer>> MAX_SUR_TIME = ATTACHMENT_TYPES.register(
            "max_sur_time", () -> AttachmentType.builder(() -> 0).serialize(Codec.INT).build());

    public static final Supplier<AttachmentType<Integer>> SUMMON_TIME = ATTACHMENT_TYPES.register(
            "summon_time", () -> AttachmentType.builder(() -> 0).serialize(Codec.INT).build());

}
