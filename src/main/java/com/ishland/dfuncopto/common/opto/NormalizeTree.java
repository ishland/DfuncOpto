package com.ishland.dfuncopto.common.opto;

import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;

public class NormalizeTree {

    public static DensityFunction normalize(DensityFunction df) {
        if (df instanceof DensityFunctionTypes.BinaryOperation op) {
            // const should be argument2
            if (op.type() == DensityFunctionTypes.BinaryOperationLike.Type.ADD || op.type() == DensityFunctionTypes.BinaryOperationLike.Type.MUL) {
                if (op.argument1() instanceof DensityFunctionTypes.Constant && !(op.argument2() instanceof DensityFunctionTypes.Constant)) {
                    return new DensityFunctionTypes.BinaryOperation(op.type(), op.argument2(), op.argument1(), op.minValue(), op.maxValue());
                }
            }
        }
        return df;
    }

}
