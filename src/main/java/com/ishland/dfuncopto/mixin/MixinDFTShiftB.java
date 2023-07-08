package com.ishland.dfuncopto.mixin;

import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DensityFunctionTypes.ShiftB.class)
public class MixinDFTShiftB {

    @Shadow @Final private DensityFunction.Noise offsetNoise;

    /**
     * @author ishland
     * @reason Reduce object allocation
     */
    @Overwrite
    public DensityFunction apply(DensityFunction.DensityFunctionVisitor visitor) {
        final DensityFunction.Noise apply = visitor.apply(this.offsetNoise);
        if (apply == this.offsetNoise) return visitor.apply((DensityFunction) this);
        return visitor.apply(new DensityFunctionTypes.Shift(apply));
    }

}
