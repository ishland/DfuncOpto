package com.ishland.dfuncopto.common;

import com.ishland.dfuncopto.common.debug.DotExporter;
import com.ishland.dfuncopto.common.opto.BreakBlending;
import com.ishland.dfuncopto.common.opto.FoldConstants;
import com.ishland.dfuncopto.common.opto.InlineHolders;
import com.ishland.dfuncopto.common.opto.InstCombine;
import com.ishland.dfuncopto.common.opto.NormalizeTree;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.world.gen.densityfunction.DensityFunction;

import java.util.function.Function;

public class OptoTransformation {

    public static DensityFunction copyAndOptimize(String name, DensityFunction df) {
        final long id = DotExporter.ID.incrementAndGet();
        Reference2ReferenceOpenHashMap<DensityFunction, DensityFunction> cloneCache = new Reference2ReferenceOpenHashMap<>();
        final DensityFunction copy = DensityFunctionUtil.deepClone(df, cloneCache);
        DotExporter.writeToDisk(id + "-" + name + "-before", copy);
        final DensityFunction optimized = optimize(name, copy,
                InlineHolders::inline,
                BreakBlending::breakBlending,
                FoldConstants::fold,
                NormalizeTree::normalize,
                InstCombine::combine
        );
        DotExporter.writeToDisk(id + "-" + name + "-after", optimized);
        return optimized;
    }

    @SafeVarargs
    private static DensityFunction optimize(String name, DensityFunction df, Function<DensityFunction, DensityFunction>... visitors) {
        int i = 0;
        while (visitNodeReplacing(df, visitors)) {
            i++;
        }
        System.out.println(String.format("Optimization finished after %d iterations for %s", i, name));
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
