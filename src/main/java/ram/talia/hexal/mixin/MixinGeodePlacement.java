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
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.GeodeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.GeodeConfiguration;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ram.talia.hexal.Hexal;
import ram.talia.hexal.api.config.HexalConfig;
import ram.talia.hexal.common.lib.HexalBlocks;

import java.util.function.Predicate;

@Mixin(GeodeFeature.class)
public abstract class MixinGeodePlacement extends Feature<GeodeConfiguration> {
    public MixinGeodePlacement(Codec<GeodeConfiguration> p_159834_) {
        super(p_159834_);
    }

    @Definition(id = "predicate", local = @Local(type = Predicate.class))
    @Expression("predicate = ?")
    @Inject(method = "place", at = @At("MIXINEXTRAS:EXPRESSION"))
    void place(FeaturePlaceContext<GeodeConfiguration> p_159836_, CallbackInfoReturnable<Boolean> cir,
               @Share("Hexal$center")LocalRef<Vec3> center, @Share("Hexal$innerCount") LocalIntRef innerCount) {
        center.set(Vec3.ZERO);
        innerCount.set(0);
    }

    @Definition(id = "d2", local = @Local(type = double.class, name = "d2"))
    @Definition(id = "d6", local = @Local(type = double.class, name = "d6"))
    @Definition(id = "d1", local = @Local(type = double.class, ordinal = 4))
    @Expression("d6 >= d1")
    @Inject(method = "place", at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.AFTER))
    void updateCounts(FeaturePlaceContext<GeodeConfiguration> p_159836_, CallbackInfoReturnable<Boolean> cir,
                      @Local(name = "blockpos3", type = BlockPos.class) BlockPos blockpos3,
                      @Share("Hexal$center")LocalRef<Vec3> center, @Share("Hexal$innerCount") LocalIntRef innerCount) {
        center.set(center.get().add(blockpos3.getCenter()));
        innerCount.set(innerCount.get() + 1);
    }

    @Inject(method = "place", at = @At("TAIL"))
    void placeSlipway(FeaturePlaceContext<GeodeConfiguration> p_159836_, CallbackInfoReturnable<Boolean> cir,
                      @Share("Hexal$center") LocalRef<Vec3> center, @Share("Hexal$innerCount") LocalIntRef innerCount) {
        Vec3 cent = center.get();
        RandomSource source = p_159836_.random();

        if (source.nextFloat() < HexalConfig.Server.SLIPWAY_CHANCE.get()) {
            cent = cent.scale(1.0 / (double)innerCount.get());
            safeSetBlock(p_159836_.level(), BlockPos.containing(cent), HexalBlocks.SLIPWAY.defaultBlockState(), a -> true);
        }
    }
}
