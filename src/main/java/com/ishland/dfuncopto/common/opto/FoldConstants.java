package com.ishland.dfuncopto.common.opto;

import com.ishland.dfuncopto.common.opto.functions.LinearFMA;
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

        // when this executes for folding constants, it should be already converted to LinearOperation
        if (df instanceof DensityFunctionTypes.LinearOperation op) {
            if (op.argument1() instanceof DensityFunctionTypes.Constant const1 && op.argument2() instanceof DensityFunctionTypes.Constant const2) {
                return switch (op.type()) {
                    case ADD -> new DensityFunctionTypes.Constant(const1.value() + const2.value());
                    case MUL -> new DensityFunctionTypes.Constant(const1.value() * const2.value());
                    case MIN -> new DensityFunctionTypes.Constant(Math.min(const1.value(), const2.value()));
                    case MAX -> new DensityFunctionTypes.Constant(Math.max(const1.value(), const2.value()));
                };
            }

            // elimination
            if (op.type() == DensityFunctionTypes.BinaryOperationLike.Type.ADD && op.argument() == 0.0) {
                return op.argument2();
            }
            if (op.type() == DensityFunctionTypes.BinaryOperationLike.Type.MUL) {
                if (op.argument() == 0.0) {
                    return new DensityFunctionTypes.Constant(0.0);
                }
                if (op.argument() == 1.0) {
                    return op.argument2();
                }
            }
        }

        if (df instanceof LinearFMA op) {
            // elimination
            if (op.add() == 0) {
                return new DensityFunctionTypes.LinearOperation(DensityFunctionTypes.LinearOperation.SpecificType.MUL, op.input(), op.minValue(), op.maxValue(), op.mul());
            }
            if (op.mul() == 1) {
                return new DensityFunctionTypes.LinearOperation(DensityFunctionTypes.LinearOperation.SpecificType.ADD, op.input(), op.minValue(), op.maxValue(), op.add());
            }
            if (op.mul() == 0) {
                return new DensityFunctionTypes.Constant(op.add());
            }

            // fold constants
            if (op.input() instanceof DensityFunctionTypes.Constant constant) {
                return new DensityFunctionTypes.Constant(constant.value() * op.mul() + op.add());
            }
        }

        return df;
    }

}
