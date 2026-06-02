package ram.talia.hexal.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.SyncedDataHolder;
import net.minecraft.network.syncher.SynchedEntityData;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ram.talia.hexal.Hexal;
import ram.talia.hexal.common.entities.BaseCastingWisp;
import ram.talia.hexal.common.entities.TickingWisp;

import java.util.Arrays;

@Mixin(SynchedEntityData.class)
public class SynchedEntityDataMixin {
    @Inject(method = "defineId", at = @At(value = "RETURN"))
    private static <T> void inject(Class<? extends SyncedDataHolder> clazz, EntityDataSerializer<T> serializer, CallbackInfoReturnable<EntityDataAccessor<T>> cir,
                                   @Local(ordinal = 0) int i) {
        //if (clazz == TickingWisp.class || clazz.getSuperclass() == BaseCastingWisp.class || clazz.getSuperclass().getSuperclass() == BaseCastingWisp.class) {
            Hexal.LOGGER.info("id {} for serializer {} of type {}.", i, "a", clazz.getSimpleName());
        //}
    }
}
