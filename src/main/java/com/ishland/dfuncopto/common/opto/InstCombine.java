package com.ishland.dfuncopto.common.opto;

import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;

public class InstCombine {

    public static DensityFunction combine(DensityFunction df) {
        if (df instanceof DensityFunctionTypes.BinaryOperation operation) {
            // Combine two additions, constant should always be arg2 (see NormalizeTree)
            if (operation.argument2() instanceof DensityFunctionTypes.Constant c1) {
                if (operation.argument1() instanceof DensityFunctionTypes.BinaryOperation a && a.argument2() instanceof DensityFunctionTypes.Constant c2) {
                    if (a.type() == DensityFunctionTypes.BinaryOperationLike.Type.ADD && operation.type() == DensityFunctionTypes.BinaryOperationLike.Type.ADD) {
                        return new DensityFunctionTypes.BinaryOperation(DensityFunctionTypes.BinaryOperationLike.Type.ADD, a.argument1(), new DensityFunctionTypes.Constant(c1.value() + c2.value()), operation.minValue(), operation.maxValue());
                    }
                    if (a.type() == DensityFunctionTypes.BinaryOperationLike.Type.MUL && operation.type() == DensityFunctionTypes.BinaryOperationLike.Type.MUL) {
                        return new DensityFunctionTypes.BinaryOperation(DensityFunctionTypes.BinaryOperationLike.Type.MUL, a.argument1(), new DensityFunctionTypes.Constant(c1.value() * c2.value()), operation.minValue(), operation.maxValue());
                    }
                }
            }
        }

        return df;
    }

}
