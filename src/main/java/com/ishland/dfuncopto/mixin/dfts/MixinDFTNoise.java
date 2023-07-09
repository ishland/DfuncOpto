package com.ishland.dfuncopto.mixin.dfts;

import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DensityFunctionTypes.Noise.class)
public class MixinDFTNoise {

    @Shadow @Final private DensityFunction.Noise noise;

    @Shadow @Final @Deprecated private double xzScale;

    @Shadow @Final private double yScale;

    /**
     * @author ishland
     * @reason Reduce object allocation
     */
    @Overwrite
    public DensityFunction apply(DensityFunction.DensityFunctionVisitor visitor) {
        final DensityFunction.Noise apply = visitor.apply(this.noise);
        if (apply == this.noise) return visitor.apply((DensityFunction) this);
        return visitor.apply(new DensityFunctionTypes.Noise(apply, this.xzScale, this.yScale));
    }

}
