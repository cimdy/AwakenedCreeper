package com.cimdy.awakenedcreeper.mixin.goal;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Ocelot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AvoidEntityGoal.class)
public class AvoidEntityGoalMixin<T extends LivingEntity> extends Goal {
    @Shadow @Final protected PathfinderMob mob;

    @Shadow @Final protected Class<T> avoidClass;

    @Inject(method = "canUse", at = @At("RETURN"), cancellable = true)
    private void CreeperAvoidCat(CallbackInfoReturnable<Boolean> cir){
        if(this.mob.getType() == EntityType.CREEPER){
            cir.setReturnValue(!(this.avoidClass == Cat.class || this.avoidClass == Ocelot.class));
        }
    }


    @Unique
    public boolean canUse() {
        return false;
    }
}
