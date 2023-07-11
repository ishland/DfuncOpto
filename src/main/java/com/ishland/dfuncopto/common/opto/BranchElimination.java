package com.ishland.dfuncopto.common.opto;

import com.ishland.dfuncopto.common.DFCacheControl;
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

        if (df instanceof DensityFunctionTypes.BinaryOperation op) {
            final DFCacheControl cacheControl = (DFCacheControl) (Object) op;
            if (op.type() == DensityFunctionTypes.BinaryOperationLike.Type.MAX || op.type() == DensityFunctionTypes.BinaryOperationLike.Type.MIN) {
                try {
                    cacheControl.dfuncopto$refreshMinMaxCache();
                    cacheControl.dfuncopto$setMinMaxCachingDisabled(false);
                    if (op.type() == DensityFunctionTypes.BinaryOperationLike.Type.MAX) {
                        if (op.argument1().minValue() >= op.argument2().maxValue()) {
                            return op.argument1();
                        }
                        if (op.argument2().minValue() >= op.argument1().maxValue()) {
                            return op.argument2();
                        }
                    } else if (op.type() == DensityFunctionTypes.BinaryOperationLike.Type.MIN) {
                        if (op.argument1().maxValue() <= op.argument2().minValue()) {
                            return op.argument1();
                        }
                        if (op.argument2().maxValue() <= op.argument1().minValue()) {
                            return op.argument2();
                        }
                    }
                } finally {
                    cacheControl.dfuncopto$setMinMaxCachingDisabled(true);
                }
            }
        }

        return df;
    }

}
