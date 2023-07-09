package com.ishland.dfuncopto.common.opto;

import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;

public class InlineHolders {

    public static DensityFunction inline(DensityFunction df) {
        if (df instanceof DensityFunctionTypes.RegistryEntryHolder holder) {
            return holder.function().value();
        }

        return df;
    }

}
