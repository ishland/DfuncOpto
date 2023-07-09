package com.ishland.dfuncopto.common;

import com.ishland.dfuncopto.common.opto.BreakBlending;
import com.ishland.dfuncopto.common.opto.FoldConstants;
import com.ishland.dfuncopto.common.opto.InlineHolders;
import com.ishland.dfuncopto.common.opto.InstCombine;
import com.ishland.dfuncopto.common.opto.NormalizeTree;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class OptoTransformation {

    public static DensityFunction copyAndOptimize(DensityFunction df) {
        return copyAndOptimize(df,
                InlineHolders::inline,
                BreakBlending::breakBlending,
                FoldConstants::fold,
                NormalizeTree::normalize,
                InstCombine::combine
        );
    }

    @SafeVarargs
    public static DensityFunction copyAndOptimize(DensityFunction df, Function<DensityFunction, DensityFunction>... visitors) {
        final DensityFunction densityFunction = DensityFunctionUtil.deepClone(df);
        return optimize(densityFunction, visitors);
    }

    @SafeVarargs
    private static DensityFunction optimize(DensityFunction df, Function<DensityFunction, DensityFunction>... visitors) {
        int i = 0;
        while (visitNodeReplacing(df, visitors)) {
            i++;
        }
        System.out.println(String.format("Optimization finished after %d iterations", i));
        return df;
    }

    /**
     * This function will visit every child node recursively, and if a replacement is found, returns immediately.
     *
     * @param df       The root node
     * @param visitors The visitors
     * @return Whether a replacement is found
     */
    private static boolean visitNodeReplacing(DensityFunction df, Function<DensityFunction, DensityFunction>[] visitors) {
        if (df instanceof IDensityFunction<?> idf) {
            final DensityFunction[] children = idf.dfuncopto$getChildren();
            for (int i = 0; i < children.length; i++) {
                final DensityFunction child = children[i];
                for (Function<DensityFunction, DensityFunction> visitor : visitors) {
                    DensityFunction apply = visitor.apply(child);
                    if (apply != null && apply != child) {
//                        System.out.println(String.format("Replacing %s", child));
//                        System.out.println(String.format("With %s", apply));
                        idf.dfuncopto$replace(child, apply);
                        return true;
                    }
                }
                if (visitNodeReplacing(child, visitors)) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

}
