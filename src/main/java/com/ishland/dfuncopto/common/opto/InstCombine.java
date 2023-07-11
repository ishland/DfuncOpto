package com.ishland.dfuncopto.common.opto;

import com.ishland.dfuncopto.common.opto.functions.LinearFMA;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;

public class InstCombine {

    public static DensityFunction combine(DensityFunction df) {
        if (df instanceof DensityFunctionTypes.LinearOperation operation) {
            // Combine two additions, constant should always be arg1
            if (operation.argument2() instanceof DensityFunctionTypes.LinearOperation inner) {
                if (inner.specificType() == DensityFunctionTypes.LinearOperation.SpecificType.ADD && operation.specificType() == DensityFunctionTypes.LinearOperation.SpecificType.ADD) {
                    return new DensityFunctionTypes.LinearOperation(DensityFunctionTypes.LinearOperation.SpecificType.ADD, inner.argument2(), operation.minValue(), operation.maxValue(), operation.argument() + inner.argument());
                }
                if (inner.specificType() == DensityFunctionTypes.LinearOperation.SpecificType.MUL && operation.specificType() == DensityFunctionTypes.LinearOperation.SpecificType.MUL) {
                    return new DensityFunctionTypes.LinearOperation(DensityFunctionTypes.LinearOperation.SpecificType.MUL, inner.argument2(), operation.minValue(), operation.maxValue(), operation.argument() * inner.argument());
                }
                if (inner.specificType() == DensityFunctionTypes.LinearOperation.SpecificType.MUL && operation.specificType() == DensityFunctionTypes.LinearOperation.SpecificType.ADD) {
                    return new LinearFMA(inner.argument2(), inner.argument(), operation.argument());
                }
            }
        }

        return df;
    }

}
