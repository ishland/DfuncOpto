package com.ishland.dfuncopto.mixin;

import com.ishland.dfuncopto.common.opto.functions.DWrapping;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net/minecraft/world/gen/noise/NoiseConfig$1")
public class MixinNoiseConfig1 {

    @Inject(method = "unwrap", at = @At("HEAD"), cancellable = true)
    private void injectUnwrap(DensityFunction densityFunction, CallbackInfoReturnable<DensityFunction> cir) {
        if (densityFunction instanceof DWrapping wrapping) {
            cir.setReturnValue(wrapping.wrapped());
            return;
        }
    }

}
