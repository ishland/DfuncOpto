package com.ishland.dfuncopto.common.opto;

import com.ishland.dfuncopto.common.IDensityFunction;
import com.ishland.dfuncopto.common.opto.functions.DWrapping;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;

import java.util.Set;

public class ExtraCaching {

    public static DensityFunction add(DensityFunction df) {
        // this is a very rare case apparently, but the code is here anyway
//        if (df instanceof DensityFunctionTypes.RangeChoice rangeChoice) {
//            // to address a special issue in vanilla here
//            // RangeChoice:
//            //   input: A
//            //   whenInRange: min(A, ...)
//            //   whenOutOfRange: ...
//            // we add a cache wrapper around A here
//
//            if (rangeChoice.input() instanceof DWrapping) {
//                return df; // don't do it twice
//            }
//
//            Set<DensityFunction> results = new ReferenceOpenHashSet<>();
//            for (DensityFunction child : ((IDensityFunction<DensityFunctionTypes.RangeChoice>) (Object) rangeChoice).dfuncopto$getChildren()) {
//                findMatchingParents(results, child, rangeChoice.input(), 3);
//            }
//            if (!results.isEmpty()) {
//                final DWrapping wrapping = new DWrapping(DWrapping.Type.Cache3D, rangeChoice.input());
//                for (DensityFunction result : results) {
//                    if (result instanceof IDensityFunction<?> idf) {
//                        idf.dfuncopto$replace(rangeChoice.input(), wrapping);
//                    }
//                }
//                // have to return a new one to restart the loop
//                return new DensityFunctionTypes.RangeChoice(
//                        wrapping,
//                        rangeChoice.minInclusive(),
//                        rangeChoice.maxExclusive(),
//                        rangeChoice.whenInRange(),
//                        rangeChoice.whenOutOfRange()
//                );
//            }
//        }

        if (df instanceof IDensityFunction<?> idf) {
            if (df instanceof DWrapping) {
                return df; // don't do it twice
            }
            for (DensityFunction child : idf.dfuncopto$getChildren()) {
                if (child instanceof DensityFunctionTypes.ShiftedNoise noise) {
                    if (noise.yScale() == 0 && noise.shiftY() instanceof DensityFunctionTypes.Constant) {
                        if (df instanceof DensityFunctionTypes.Wrapping wrapping && wrapping.type() == DensityFunctionTypes.Wrapping.Type.CACHE2D) {
                            continue; // don't do it twice
                        }
                        // y doesn't matter
                        idf.dfuncopto$replace(child, new DWrapping(DWrapping.Type.Cache2D, child));
                        return df;
                    }
                }
                if (child instanceof DensityFunctionTypes.Noise noise) {
                    if (noise.yScale() == 0) {
                        if (df instanceof DensityFunctionTypes.Wrapping wrapping && wrapping.type() == DensityFunctionTypes.Wrapping.Type.CACHE2D) {
                            continue; // don't do it twice
                        }
                        // y doesn't matter
                        idf.dfuncopto$replace(child, new DWrapping(DWrapping.Type.Cache2D, child));
                        return df;
                    }
                }
            }
        }

        return df;
    }

    private static void findMatchingParents(Set<DensityFunction> result, DensityFunction current, DensityFunction target, int maxDepth) {
        if (maxDepth == 0) return;
        if (current instanceof IDensityFunction<?> idf) {
            for (DensityFunction child : idf.dfuncopto$getChildren()) {
                if (child == target) {
                    result.add(current);
                } else {
                    findMatchingParents(result, child, target, maxDepth - 1);
                }
            }
        }
    }

}
