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
            {
                DFCacheControl cacheControl = op.input() instanceof DFCacheControl ctrl ? ctrl : null;
                double minValue;
                double maxValue;
                try {
                    if (cacheControl != null) {
                        cacheControl.dfuncopto$refreshMinMaxCache();
                        cacheControl.dfuncopto$setMinMaxCachingDisabled(false);
                    }
                    minValue = op.input().minValue();
                    maxValue = op.input().maxValue();
                } finally {
                    if (cacheControl != null) {
                        cacheControl.dfuncopto$setMinMaxCachingDisabled(true);
                    }
                }
                if (minValue >= op.minInclusive() && maxValue < op.maxExclusive()) {
                    return op.whenInRange();
                }
                if (minValue >= op.maxExclusive() || maxValue < op.minInclusive()) {
                    return op.whenOutOfRange();
                }
            }
        }

        if (df instanceof DensityFunctionTypes.BinaryOperation op) {
            if (op.type() == DensityFunctionTypes.BinaryOperationLike.Type.MAX || op.type() == DensityFunctionTypes.BinaryOperationLike.Type.MIN) {
                DFCacheControl ctrl1 = op.argument1() instanceof DFCacheControl ctrl ? ctrl : null;
                DFCacheControl ctrl2 = op.argument2() instanceof DFCacheControl ctrl ? ctrl : null;
                try {
                    if (ctrl1 != null) {
                        ctrl1.dfuncopto$refreshMinMaxCache();
                        ctrl1.dfuncopto$setMinMaxCachingDisabled(false);
                    }
                    if (ctrl2 != null) {
                        ctrl2.dfuncopto$refreshMinMaxCache();
                        ctrl2.dfuncopto$setMinMaxCachingDisabled(false);
                    }

                    final double arg1min = op.argument1().minValue();
                    final double arg1max = op.argument1().maxValue();
                    final double arg2min = op.argument2().minValue();
                    final double arg2max = op.argument2().maxValue();
                    if (op.type() == DensityFunctionTypes.BinaryOperationLike.Type.MAX) {
                        if (arg1min >= arg2max) {
                            return op.argument1();
                        }
                        if (arg2min >= arg1max) {
                            return op.argument2();
                        }
                    } else if (op.type() == DensityFunctionTypes.BinaryOperationLike.Type.MIN) {
                        if (arg1max <= arg2min) {
                            return op.argument1();
                        }
                        if (arg2max <= arg1min) {
                            return op.argument2();
                        }
                    }
                } finally {
                    if (ctrl1 != null) ctrl1.dfuncopto$setMinMaxCachingDisabled(true);
                    if (ctrl2 != null) ctrl2.dfuncopto$setMinMaxCachingDisabled(true);
                }
            }
        }

        return df;
    }

}
