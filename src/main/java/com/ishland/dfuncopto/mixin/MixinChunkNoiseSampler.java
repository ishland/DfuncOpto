package com.ishland.dfuncopto.mixin;

import com.ishland.dfuncopto.common.opto.functions.Cache3D;
import com.ishland.dfuncopto.common.opto.functions.DWrapping;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkNoiseSampler.class)
public class MixinChunkNoiseSampler {

    @Inject(method = "getActualDensityFunctionImpl", at = @At("HEAD"), cancellable = true)
    private void injectCaching(DensityFunction densityFunction, CallbackInfoReturnable<DensityFunction> cir) {
        if (densityFunction instanceof DWrapping wrapping) {
            if (wrapping.type() == DWrapping.Type.Cache3D) {
                cir.setReturnValue(new Cache3D(wrapping.wrapped()));
                return;
            }
            if (wrapping.type() == DWrapping.Type.Cache2D) {
                cir.setReturnValue(new ChunkNoiseSampler.Cache2D(wrapping.wrapped()));
                return;
            }
        }
    }

}
