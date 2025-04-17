package com.cimdy.awakenedcreeper.mixin;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.*;

import static com.cimdy.awakenedcreeper.event.CreeperEvent.getPerHealth;

@Mixin(Creeper.class)
public abstract class CreeperMixin extends Monster {
    @Shadow private int swell;
    @Shadow private int explosionRadius;
    @Shadow public abstract boolean isPowered();
    @Shadow private int oldSwell;
    @Shadow public abstract void setSwellDir(int pState);
    @Shadow public abstract float getSwelling(float pPartialTicks);

    @Shadow @Final private static EntityDataAccessor<Boolean> DATA_IS_IGNITED;

    protected CreeperMixin(EntityType<? extends Monster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    /**
     * @author cimdy
     * @reason xxxx
     */
    @Overwrite
    private void explodeCreeper(){
        if (this.level() instanceof ServerLevel serverLevel) {
            float f = this.isPowered() ? 2.0F : 1.0F; //闪电苦力怕伤害加成
            //低血逐渐降低几率 20%血 仅有20%几率不死
            if (serverLevel.random.nextInt(100) + 1 > 100 - getPerHealth(this)) {
                //爆炸若判断不死 获得伤害吸收和生命回复
                this.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 5 * 20, 2));
                this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 10 * 20, 2));
                this.swell = 0;
                this.oldSwell = 0;
                this.setSwellDir(-1);
                this.entityData.set(DATA_IS_IGNITED, false);//刷新各种状态
                getSwelling(0);//刷新爆炸时苦力怕的闪光效果
            } else {
                //若因爆炸死亡获得更高爆炸伤害 随低血降低
                f = f * ((100 - getPerHealth(this)) + 100) / 100;
                this.dead = true;
                this.discard();
            }
            this.level().explode(this, this.getX(), this.getY(), this.getZ(),
                    (float) this.explosionRadius * f, Level.ExplosionInteraction.MOB);
        }
    }


}
