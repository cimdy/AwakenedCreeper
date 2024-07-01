package com.cimdy.awakenedcreeper.mixin;

import com.cimdy.awakenedcreeper.attach.AttachRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Creeper.class)
public abstract class CreeperMixin extends Monster implements PowerableMob {
    //已损失血量百分比
    @Unique
    int awakenedCreeper_NeoForge_1_21_0$damage = (int)((this.getMaxHealth() - this.getHealth()) / this.getMaxHealth() * 100);
    //获取区域难度和副区域难度
    //区域难度在简单难度中区域难度的范围是0.75–1.5，在普通难度时为1.5–4.0，在困难难度时为2.25–6.75
    @Unique
    float regional_difficulty =  this.level().getCurrentDifficultyAt(this.getOnPos()).getEffectiveDifficulty();
    //副区域难度是介于0.00和1.00之间的值
    //在简单难度中区域难度没有影响；普通难度中且满月时在一个区块中游玩21小时将达到最大影响；
    //而困难难度中且满月时在区块中只要游玩4 1⁄6小时就能达到最大影响，并且在游玩超过16 2⁄3小时时将保持最大影响。
    @Unique
    float clamped_regional_difficulty =  this.level().getCurrentDifficultyAt(this.getOnPos()).getSpecialMultiplier();
    //难度系数
    @Unique
    float awakenedCreeper_NeoForge_1_21_0$difficultyMultiplier = regional_difficulty * (1 + clamped_regional_difficulty) / 3 * 2;// 1 - 9  -> 1 - 5

    @Shadow private int swell;

    @Shadow private int explosionRadius;

    @Shadow public abstract boolean isPowered();

    @Shadow private int oldSwell;

    @Shadow public abstract void setSwellDir(int pState);

    @Shadow public abstract float getSwelling(float pPartialTicks);

    @Shadow public abstract boolean doHurtTarget(Entity pEntity);

    @Shadow @Final private static EntityDataAccessor<Boolean> DATA_IS_IGNITED;

    protected CreeperMixin(EntityType<? extends Monster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tick(CallbackInfo ci){

        if(this.swell == 1){//开始蓄力时进行一次随机
            int random = (int)(Math.random() * 100 + 1);
            random = random + awakenedCreeper_NeoForge_1_21_0$damage; //低血增加几率 50%血以下必定触发30%缩短 10%血则几乎瞬爆
            if(50 <= random && random <= 89){
                this.swell += 10;//苦力怕有50%的几率使 爆炸蓄力时间 缩短30%
            }else if(90 <= random){
                this.swell += 20;//苦力怕有10%的几率使 爆炸蓄力时间 缩短60%
            }
        }

        int MaxSurTime = this.getData(AttachRegister.MAX_SUR_TIME);//获取存活时间
        if(MaxSurTime == 0){//若为0，则默认值 使其为-1 无限存活
            MaxSurTime = -1;
        }else if(MaxSurTime > 1){//若大于1，则 使其每Tick -1
            MaxSurTime = MaxSurTime -1;
            if(this.swell >= 1){
                this.swell = this.swell + 1; //具有常驻双倍爆炸速度
            }
        }else if(MaxSurTime == 1){//若等于1 实体消失
            this.dead = true;
            this.discard();
        }

        if (MaxSurTime == -1){//当为-1时 为自然生成苦力怕 执行生成苦力怕检测
            int SummonTime = this.getData(AttachRegister.SUMMON_TIME);
            int MaxSummonTime = (int) (1200 / awakenedCreeper_NeoForge_1_21_0$difficultyMultiplier * 10);
        
            SummonTime = SummonTime + 1;
            if(SummonTime >= MaxSummonTime){
                boolean summon = false;
                if(this.level().hasNearbyAlivePlayer(Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ()), 32.0)){//附近32格内存在玩家 则判断生成苦力怕
                    //系数最大为 5 最小为 1
                    if(awakenedCreeper_NeoForge_1_21_0$difficultyMultiplier > 5.0F){
                        awakenedCreeper_NeoForge_1_21_0$difficultyMultiplier = 5.0F;
                    }
                    if(awakenedCreeper_NeoForge_1_21_0$difficultyMultiplier < 1.0F){
                        awakenedCreeper_NeoForge_1_21_0$difficultyMultiplier = 1.0F;
                    }
                    awakenedCreeper_NeoForge_1_21_0$summonCreeper(//生成的数量 苦力怕基础移速 基础生命 持续时间与 难度 成正比
                            awakenedCreeper_NeoForge_1_21_0$difficultyMultiplier, awakenedCreeper_NeoForge_1_21_0$difficultyMultiplier / 10,
                            awakenedCreeper_NeoForge_1_21_0$difficultyMultiplier / 10, awakenedCreeper_NeoForge_1_21_0$difficultyMultiplier / 10);
                    summon = true;
                }
                if(summon | SummonTime - MaxSummonTime > MaxSummonTime){//当已经生成 或计时器大于两倍计时器时 重置计时器
                    SummonTime = 0;
                }
            }
            this.setData(AttachRegister.SUMMON_TIME,SummonTime);
        }
        this.setData(AttachRegister.MAX_SUR_TIME,MaxSurTime);
    }

    @Inject(method = "getHurtSound",at = @At("RETURN"))
    private void getHurtSound(DamageSource pDamageSource, CallbackInfoReturnable<SoundEvent> cir){
        if(this.swell == 0 && pDamageSource.getEntity() instanceof LivingEntity){//非蓄力,且受伤,且被实体击伤
            int random = (int)(Math.random() * 100 + 1);
            random = random + awakenedCreeper_NeoForge_1_21_0$damage;//低血逐渐增加几率 50%血以下必定触发移速 10%血则必定获得爆发性移速
            if(50 <= random && random <= 89){
                this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,5 * 20, 3));//被击几率增加移动速度
            }else if(90 <= random){
                this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,3 * 20, 5));//更高的爆发性移速
            }
        }
    }

    @Unique
    private void awakenedCreeper_NeoForge_1_21_0$summonCreeper(float SumNumber, float MoveSpeedMultiplier, float MaxHealthMultiplier, float MaxSurTime) {//召唤苦力怕小弟
        if(!this.level().isClientSide){
            for(int l = 0; l < 50; ++l) {
                LivingEntity livingentity = this.getTarget();
                ServerLevel serverLevel = (ServerLevel) this.level();
                int i = Mth.floor(this.getX());
                int j = Mth.floor(this.getY());
                int k = Mth.floor(this.getZ());//获取苦力怕坐标
                int i1 = i + Mth.nextInt(this.random, 7, 40) * Mth.nextInt(this.random, -1, 1);
                int j1 = j + Mth.nextInt(this.random, 7, 40) * Mth.nextInt(this.random, -1, 1);
                int k1 = k + Mth.nextInt(this.random, 7, 40) * Mth.nextInt(this.random, -1, 1);//获取一个随机坐标
                BlockPos blockpos = new BlockPos(i1, j1, k1);
                EntityType<?> entityType = this.getType();
                Creeper creeper = EntityType.CREEPER.create(this.level());
                if (SpawnPlacements.isSpawnPositionOk(entityType, this.level(), blockpos)
                        && SpawnPlacements.checkSpawnRules(entityType, serverLevel, MobSpawnType.REINFORCEMENT, blockpos, this.level().random)) {//尝试生成
                    if (creeper != null) {
                        creeper.setPos(i1, j1, k1);
                        if (!this.level().hasNearbyAlivePlayer(i1, j1, k1, 7.0)//附近不存在玩家
                                && this.level().isUnobstructed(creeper)
                                && this.level().noCollision(creeper)
                                && !this.level().containsAnyLiquid(creeper.getBoundingBox())) {//无各种碰撞箱
                            if (livingentity != null) {//添加实体标签
                                creeper.setTarget(livingentity);
                            }
                            creeper.finalizeSpawn(//生成
                                    serverLevel, this.level().getCurrentDifficultyAt(creeper.blockPosition()), MobSpawnType.REINFORCEMENT, null
                            );
                            serverLevel.addFreshEntityWithPassengers(creeper);
                            creeper.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(creeper.getAttributeBaseValue(Attributes.MOVEMENT_SPEED) * 1.25F * (1 + MoveSpeedMultiplier));//新苦力怕 有更快移动速度
                            creeper.getAttribute(Attributes.MAX_HEALTH).setBaseValue(creeper.getAttributeBaseValue(Attributes.MAX_HEALTH) * 0.5F * (1 + MaxHealthMultiplier));//但是生命值降低
                            creeper.setData(AttachRegister.MAX_SUR_TIME, (int)(400 * (1 + MaxSurTime)));//最大生存时间
                        }
                    }
                }
                SumNumber = SumNumber -1;
                if(SumNumber == 0 ){//苦力怕生成结束
                    break;//结束生成循环
                }
            }
        }
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    private void explodeCreeper(){
        if (!this.level().isClientSide) {
            float f = this.isPowered() ? 2.0F : 1.0F;//闪电苦力怕伤害加成
            int MaxSurTime = this.getData(AttachRegister.MAX_SUR_TIME);//获取存活时间
            boolean reborn = false;
            if(MaxSurTime == -1){//仅自然生成苦力怕具有爆炸不死概率
                reborn = awakenedCreeper_NeoForge_1_21_0$reborn();
                f = f * ((100 - awakenedCreeper_NeoForge_1_21_0$damage) + 100) / 100; //若因爆炸死亡获得更高爆炸伤害 随低血降低
            }
            if (reborn) {
                this.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 5 * 20, 2)); //爆炸若判断 就真的炸不死了 不死 获得伤害吸收和生命回复
                this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 10 * 20, 2));
                this.swell = 0;
                this.oldSwell = 0;
                this.setSwellDir(-1);
                this.entityData.set(DATA_IS_IGNITED,false);//刷新各种状态
                getSwelling(0);//刷新爆炸时苦力怕的闪光效果
            } else {
                this.dead = true;
                this.discard();
            }
            this.level().explode(this, this.getX(), this.getY(), this.getZ(), (float) this.explosionRadius * f, Level.ExplosionInteraction.MOB);
        }
    }

    @Unique
    public boolean awakenedCreeper_NeoForge_1_21_0$reborn(){
        int random = (int)(Math.random() * 100 + 1);
        random = random - (awakenedCreeper_NeoForge_1_21_0$damage / 4);//低血逐渐降低几率 20%血 仅有20%几率不死
        return 60 <= random; //几率不死
    }

}
