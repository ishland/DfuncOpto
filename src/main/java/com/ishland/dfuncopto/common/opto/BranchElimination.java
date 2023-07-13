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
            if (op.input().minValue() >= op.minInclusive() && op.input().maxValue() < op.maxExclusive()) {
                return op.whenInRange();
            }
            if (op.input().minValue() >= op.maxExclusive() || op.input().maxValue() < op.minInclusive()) {
                return op.whenOutOfRange();
            }
        }

        if (df instanceof DensityFunctionTypes.BinaryOperation op) {
            if (op.type() == DensityFunctionTypes.BinaryOperationLike.Type.MAX || op.type() == DensityFunctionTypes.BinaryOperationLike.Type.MIN) {
                final double arg1min = op.argument1().minValue();
                final double arg1max = op.argument1().maxValue();
                final double arg2min = op.argument2().minValue();
                final double arg2max = op.argument2().maxValue();
                if (op.type() == DensityFunctionTypes.BinaryOperationLike.Type.MAX) {
                    if (arg1min > arg2max) {
                        return op.argument1();
                    }
                    if (arg2min > arg1max) {
                        return op.argument2();
                    }
                } else if (op.type() == DensityFunctionTypes.BinaryOperationLike.Type.MIN) {
                    if (arg1max < arg2min) {
                        return op.argument1();
                    }
                    if (arg2max < arg1min) {
                        return op.argument2();
                    }
                }
            }
        }

        return df;
    }

}
