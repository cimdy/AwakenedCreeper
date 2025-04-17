package com.cimdy.awakenedcreeper.event;

import com.cimdy.awakenedcreeper.AwakenedCreeper;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.item.enchantment.effects.*;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.*;
import net.minecraft.world.level.storage.loot.providers.number.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class GenEvent {
    public static ResourceKey<Enchantment> UNSTABLE_CHANNELING =
            ResourceKey.create(Registries.ENCHANTMENT,
                    ResourceLocation.fromNamespaceAndPath(AwakenedCreeper.MODID, "unstable_channeling"));

    public static String UNSTABLE_CHANNELING_COMPONENT_STRING =
            "enchantment." + AwakenedCreeper.MODID + ".unstable_channeling";
    @SubscribeEvent
    public static void ServerGatherDataEvent(GatherDataEvent.Client event) {
        event.getGenerator()
                .addProvider(true,
                        new DatapackBuiltinEntriesProvider(
                                event.getGenerator().getPackOutput(),
                                event.getLookupProvider(),
                                new RegistrySetBuilder()
                                        .add(Registries.ENCHANTMENT, bootstrap  -> {
                                            HolderGetter<Item> holdergetter = bootstrap.lookup(Registries.ITEM);
                                            HolderGetter<EntityType<?>> holdergetter1 = bootstrap.lookup(Registries.ENTITY_TYPE);
                                            bootstrap .register(
                                                    UNSTABLE_CHANNELING,
                                                    new Enchantment(
                                                            Component.translatable(UNSTABLE_CHANNELING_COMPONENT_STRING),
                                                            new Enchantment.EnchantmentDefinition(
                                                                    holdergetter.getOrThrow(ItemTags.TRIDENT_ENCHANTABLE),
                                                                    Optional.empty(),
                                                                    1,
                                                                    1,
                                                                    Enchantment.constantCost(25),
                                                                    Enchantment.constantCost(50),
                                                                    8,
                                                                    List.of(EquipmentSlotGroup.MAINHAND)
                                                            ),
                                                            HolderSet.empty(),
                                                            DataComponentMap.builder()
                                                                    .set(
                                                                            EnchantmentEffectComponents.POST_ATTACK,
                                                                            List.of(
                                                                                    new TargetedConditionalEffect<>(
                                                                                    EnchantmentTarget.ATTACKER,
                                                                                    EnchantmentTarget.VICTIM,
                                                                                    AllOf.entityEffects(
                                                                                            new SummonEntityEffect(HolderSet.direct(EntityType.LIGHTNING_BOLT.builtInRegistryHolder()), false),
                                                                                            new PlaySoundEffect(SoundEvents.TRIDENT_THUNDER, ConstantFloat.of(5.0F), ConstantFloat.of(1.0F))
                                                                                    ),
                                                                                    Optional.of(
                                                                                            AllOfCondition.allOf(
                                                                                                    WeatherCheck.weather().setRaining(true).setThundering(false),
                                                                                                    ValueCheckCondition.hasValue(new UniformGenerator(new ConstantValue(0), new ConstantValue(100)), IntRange.lowerBound(50)),
                                                                                                    LootItemEntityPropertyCondition.hasProperties(
                                                                                                            LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().located(LocationPredicate.Builder.location().setCanSeeSky(true))
                                                                                                    ),
                                                                                                    LootItemEntityPropertyCondition.hasProperties(
                                                                                                            LootContext.EntityTarget.DIRECT_ATTACKER, EntityPredicate.Builder.entity().of(holdergetter1, EntityType.TRIDENT)
                                                                                                    )
                                                                                            ).build())),
                                                                                    new TargetedConditionalEffect<>(
                                                                                            EnchantmentTarget.ATTACKER,
                                                                                            EnchantmentTarget.ATTACKER,
                                                                                            AllOf.entityEffects(
                                                                                                    new SummonEntityEffect(HolderSet.direct(EntityType.LIGHTNING_BOLT.builtInRegistryHolder()), false),
                                                                                                    new PlaySoundEffect(SoundEvents.TRIDENT_THUNDER, ConstantFloat.of(5.0F), ConstantFloat.of(1.0F))
                                                                                            ),
                                                                                            Optional.of(
                                                                                                    AllOfCondition.allOf(
                                                                                                            WeatherCheck.weather().setRaining(true).setThundering(false),
                                                                                                            ValueCheckCondition.hasValue(new UniformGenerator(new ConstantValue(0), new ConstantValue(100)), IntRange.lowerBound(50)),
                                                                                                            LootItemEntityPropertyCondition.hasProperties(
                                                                                                                    LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().located(LocationPredicate.Builder.location().setCanSeeSky(true))
                                                                                                            ),
                                                                                                            LootItemEntityPropertyCondition.hasProperties(
                                                                                                                    LootContext.EntityTarget.DIRECT_ATTACKER, EntityPredicate.Builder.entity().of(holdergetter1, EntityType.TRIDENT)
                                                                                                            )
                                                                                                    ).build())))).build()
                                                    )
                                            );
                                        }),
                                Set.of(AwakenedCreeper.MODID)
                        )
                );
        event.getGenerator().addProvider(true, new LanguageProvider(
                event.getGenerator().getPackOutput(), AwakenedCreeper.MODID, "zh_cn") {
            @Override
            protected void addTranslations() {
                this.add(UNSTABLE_CHANNELING_COMPONENT_STRING, "不稳定引雷");
            }
        });

        event.getGenerator().addProvider(true, new LanguageProvider(
                event.getGenerator().getPackOutput(), AwakenedCreeper.MODID, "en_us") {
            @Override
            protected void addTranslations() {
                this.add(UNSTABLE_CHANNELING_COMPONENT_STRING, "Unstable Channeling");
            }
        });
    }
}
