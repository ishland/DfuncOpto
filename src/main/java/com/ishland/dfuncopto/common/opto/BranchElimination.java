package com.ishland.dfuncopto.common.opto;

import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;

public class BranchElimination {

    public static DensityFunction eliminate(DensityFunction df) {
        if (df instanceof DensityFunctionTypes.RangeChoice op) {
            if (op.input() instanceof DensityFunctionTypes.Constant constant) {
                return (constant.value() >= op.minInclusive() && constant.value() < op.maxExclusive())
                        ? op.whenInRange() : op.whenOutOfRange();
            }
            if (op.whenInRange() == op.whenOutOfRange()) {
                return op.whenInRange();
            }
        }

        return df;
    }

}
