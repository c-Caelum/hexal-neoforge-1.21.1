package ram.talia.hexal.mixin;


import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.GeodeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.GeodeConfiguration;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ram.talia.hexal.Hexal;
import ram.talia.hexal.api.config.HexalConfig;
import ram.talia.hexal.common.lib.HexalBlocks;

@Mixin(value = GeodeFeature.class, remap = true)
public abstract class MixinGeodePlacement extends Feature<GeodeConfiguration> {
    public MixinGeodePlacement(Codec<GeodeConfiguration> p_159834_) {
        super(p_159834_);
    }

    @Inject(method = "place", at = @At("HEAD"), remap = true)
    void place(FeaturePlaceContext<GeodeConfiguration> p_159836_, CallbackInfoReturnable<Boolean> cir,
               @Share("Hexal$center")LocalRef<Vec3> center, @Share("Hexal$innerCount") LocalIntRef innerCount) {
        center.set(Vec3.ZERO);
        innerCount.set(0);
    }

    @Inject(method="place", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/feature/GeodeFeature;safeSetBlock(Lnet/minecraft/world/level/WorldGenLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Ljava/util/function/Predicate;)V", ordinal = 1, remap = true), remap = true)
    void counts(FeaturePlaceContext<GeodeConfiguration> p_159836_, CallbackInfoReturnable<Boolean> cir,
                @Local(name = "blockpos3") BlockPos blockpos3,
                @Share("Hexal$center") LocalRef<Vec3> center, @Share("Hexal$innerCount") LocalIntRef innerCount) {
        center.set(center.get().add(blockpos3.getCenter()));
        innerCount.set(innerCount.get() + 1);
    }

    @Inject(method = "place", at = @At("TAIL"), remap = true)
    void placeSlipway(FeaturePlaceContext<GeodeConfiguration> p_159836_, CallbackInfoReturnable<Boolean> cir,
                      @Share("Hexal$center") LocalRef<Vec3> center, @Share("Hexal$innerCount") LocalIntRef innerCount) {
        Vec3 cent = center.get();
        RandomSource source = p_159836_.random();
        if (source.nextFloat() < HexalConfig.Server.SLIPWAY_CHANCE.get()) {
            cent = cent.scale(1.0 / (double)innerCount.get());
            safeSetBlock(p_159836_.level(), BlockPos.containing(cent), HexalBlocks.SLIPWAY.defaultBlockState(), a -> true);
            //safeSetBlock(p_159836_.level(), p_159836_.origin(), HexalBlocks.SLIPWAY.defaultBlockState(), a -> true);
        }
    }
}
