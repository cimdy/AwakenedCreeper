package com.cimdy.awakenedcreeper.event;

import com.cimdy.awakenedcreeper.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;


public class CreeperEvent {
    public static void onCreeperPower(EntityTickEvent.Post event) {
        if(event.getEntity() instanceof Creeper creeper && creeper.level() instanceof ServerLevel serverLevel
                && Config.thunderPower) {
            if(!creeper.isPowered() && isInRain(creeper) && serverLevel.isThundering()) {
                LightningBolt lightningbolt = new LightningBolt(EntityType.LIGHTNING_BOLT, serverLevel);
                lightningbolt.setPos(creeper.getX(), creeper.getY(), creeper.getZ());
                serverLevel.addFreshEntity(lightningbolt);
            }
        }
    }

    public static void onCreeperExplode(EntityTickEvent.Post event) {
        if(event.getEntity() instanceof Creeper creeper && creeper.level() instanceof ServerLevel serverLevel) {
            int swell = creeper.getSwellDir();
            if(swell == 1){//开始蓄力时进行一次随机
                int random = serverLevel.getRandom().nextInt(100)
                        + getPerHealth(creeper); // 低血增加几率 50%血以下必定触发30%缩短 10%血则几乎瞬爆
                if(50 <= random && random <= 89){
                    swell += 10;//苦力怕有50%的几率使 爆炸蓄力时间 缩短30%
                }else if(90 <= random){
                    swell += 20;//苦力怕有10%的几率使 爆炸蓄力时间 缩短60%
                }
            }
            creeper.setSwellDir(swell);
        }
    }

    public static void onCreeperByHurt(LivingDamageEvent.Post event) {
        if(event.getEntity() instanceof Creeper creeper && creeper.level() instanceof ServerLevel serverLevel
                && event.getSource().getEntity() instanceof LivingEntity) { //苦力怕被实体击伤
            int swell = creeper.getSwellDir();
            if(swell == -1) { //非蓄力
                int random = serverLevel.getRandom().nextInt(100) + 1
                        + getPerHealth(creeper); //低血逐渐增加几率 50%血以下必定触发移速 10%血则必定获得爆发性移速
                if(50 <= random && random <= 89){
                    creeper.addEffect(new MobEffectInstance(MobEffects.SPEED,5 * 20, 3));//被击几率增加移动速度
                }else if(90 <= random){
                    creeper.addEffect(new MobEffectInstance(MobEffects.SPEED,3 * 20, 5));//更高的爆发性移速
                }
            }
        }
    }

    public static void onCreeperByKill(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof Creeper creeper && creeper.isPowered()
                && creeper.level() instanceof ServerLevel serverLevel
                && serverLevel.getRandom().nextInt(100) < Config.dropChance) {
            ItemStack book = Items.ENCHANTED_BOOK.getDefaultInstance();
            book.enchant(serverLevel.holderOrThrow(GenEvent.UNSTABLE_CHANNELING), 1);
            creeper.spawnAtLocation(serverLevel, book);
        }
    }

    public static int getPerHealth(LivingEntity living) {
        return (int)((living.getMaxHealth() - living.getHealth()) / living.getMaxHealth() * 100);
    }

    public static boolean isInRain(LivingEntity living) {
        BlockPos blockpos = living.blockPosition();
        return living.level().isRainingAt(blockpos)
                || living.level().isRainingAt(BlockPos.containing(blockpos.getX(), living.getBoundingBox().maxY, blockpos.getZ()));
    }
}
