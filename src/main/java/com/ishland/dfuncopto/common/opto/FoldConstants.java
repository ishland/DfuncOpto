package com.ishland.dfuncopto.common.opto;

import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;

public class FoldConstants {

    public static DensityFunction fold(DensityFunction df) {
        if (df instanceof DensityFunctionTypes.Wrapping wrapping && wrapping.wrapped() instanceof DensityFunctionTypes.Constant constant) {
            return constant;
        }

        if (df instanceof DensityFunctionTypes.Noise op && op.noise().noise() == null) {
            return new DensityFunctionTypes.Constant(0.0);
        }

        if (df instanceof DensityFunctionTypes.Shift op && op.offsetNoise().noise() == null) {
            return new DensityFunctionTypes.Constant(0.0);
        }

        if (df instanceof DensityFunctionTypes.ShiftA op && op.offsetNoise().noise() == null) {
            return new DensityFunctionTypes.Constant(0.0);
        }

        if (df instanceof DensityFunctionTypes.ShiftB op && op.offsetNoise().noise() == null) {
            return new DensityFunctionTypes.Constant(0.0);
        }

        if (df instanceof DensityFunctionTypes.ShiftedNoise op && op.noise().noise() == null) {
            return new DensityFunctionTypes.Constant(0.0);
        }

        if (df instanceof DensityFunctionTypes.BinaryOperation op) {
            if (op.argument1() instanceof DensityFunctionTypes.Constant const1 && op.argument2() instanceof DensityFunctionTypes.Constant const2) {
                return switch (op.type()) {
                    case ADD -> new DensityFunctionTypes.Constant(const1.value() + const2.value());
                    case MUL -> new DensityFunctionTypes.Constant(const1.value() * const2.value());
                    case MIN -> new DensityFunctionTypes.Constant(Math.min(const1.value(), const2.value()));
                    case MAX -> new DensityFunctionTypes.Constant(Math.max(const1.value(), const2.value()));
                };
            }
            // constant should always be arg2 (see NormalizeTree)
            if (op.argument2() instanceof DensityFunctionTypes.Constant constant) {
                if (op.type() == DensityFunctionTypes.BinaryOperationLike.Type.ADD && constant.value() == 0.0) {
                    return op.argument1();
                }
                if (op.type() == DensityFunctionTypes.BinaryOperationLike.Type.MUL) {
                    if (constant.value() == 0.0) {
                        return new DensityFunctionTypes.Constant(0.0);
                    }
                    if (constant.value() == 1.0) {
                        return op.argument1();
                    }
                }
            }
        }

        return df;
    }

}
