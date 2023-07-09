package com.ishland.dfuncopto.common.opto;

import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;

public class BreakBlending {

    public static DensityFunction breakBlending(DensityFunction df) {
        if (df == DensityFunctionTypes.BlendAlpha.INSTANCE) {
            return new DensityFunctionTypes.Constant(1.0);
        }

        if (df == DensityFunctionTypes.BlendOffset.INSTANCE) {
            return new DensityFunctionTypes.Constant(0.0);
        }

        if (df instanceof DensityFunctionTypes.BlendDensity blendDensity) {
            return blendDensity.input();
        }

        return df;
    }

}
