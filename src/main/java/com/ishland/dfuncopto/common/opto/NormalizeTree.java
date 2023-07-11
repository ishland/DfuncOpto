package com.ishland.dfuncopto.common.opto;

import com.ishland.dfuncopto.common.DFCacheControl;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;

public class NormalizeTree {

    public static DensityFunction normalize(DensityFunction df) {
        if (df instanceof DensityFunctionTypes.BinaryOperation op) {
            try {
                ((DFCacheControl) (Object) op).dfuncopto$setMinMaxCachingDisabled(false);
                DensityFunctionTypes.BinaryOperation swappedOp = op;
                // put constant to arg1
                if (!(op.argument1() instanceof DensityFunctionTypes.Constant) && op.argument2() instanceof DensityFunctionTypes.Constant) {
                    swappedOp = new DensityFunctionTypes.BinaryOperation(op.type(), op.argument2(), op.argument1(), op.minValue(), op.maxValue());
                }
//            final DensityFunctionTypes.BinaryOperationLike ret = DensityFunctionTypes.BinaryOperationLike.create(op.type(), swappedOp.argument1(), swappedOp.argument2());
//            if (ret.type() == op.type() && ret.argument1() == op.argument1() && ret.argument2() == op.argument2()) {
//                return op;
//            } else {
//                return ret;
//            }
                if (op.argument1() instanceof DensityFunctionTypes.Constant const1) {
                    return switch (op.type()) {
                        case ADD -> new DensityFunctionTypes.LinearOperation(DensityFunctionTypes.LinearOperation.SpecificType.ADD, op.argument2(), op.minValue(), op.maxValue(), const1.value());
                        case MUL -> new DensityFunctionTypes.LinearOperation(DensityFunctionTypes.LinearOperation.SpecificType.MUL, op.argument2(), op.minValue(), op.maxValue(), const1.value());
                        case MIN, MAX -> swappedOp;
                    };
                }
                return swappedOp;
            } finally {
                ((DFCacheControl) (Object) op).dfuncopto$setMinMaxCachingDisabled(true);
            }
        }
        return df;
    }

}
