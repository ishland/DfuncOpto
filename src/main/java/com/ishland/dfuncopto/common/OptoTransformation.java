package com.ishland.dfuncopto.common;

import com.google.common.base.Stopwatch;
import com.ishland.dfuncopto.common.debug.DotExporter;
import com.ishland.dfuncopto.common.opto.BreakBlending;
import com.ishland.dfuncopto.common.opto.FoldConstants;
import com.ishland.dfuncopto.common.opto.InlineHolders;
import com.ishland.dfuncopto.common.opto.InstCombine;
import com.ishland.dfuncopto.common.opto.NormalizeTree;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.world.gen.densityfunction.DensityFunction;

import java.util.Objects;
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
        DotExporter.writeToDisk(id + "-" + name + "-stage1", copy);
        deduplicate(optimized);
        DotExporter.writeToDisk(id + "-" + name + "-stage2", optimized);
        return optimized;
    }

    @SafeVarargs
    private static DensityFunction optimize(String name, DensityFunction df, Function<DensityFunction, DensityFunction>... visitors) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        int i = 0;
        while (visitNodeReplacing(df, visitors)) {
            i++;
        }
        stopwatch.stop();
        System.out.println(String.format("Optimization finished after %d iterations for %s after %s", i, name, stopwatch));
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

    private static void deduplicate(DensityFunction df) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        // map: distance from leaf -> set of nodes
        // a single node can be with multiple different distances
        ObjectSet<NodeAndItsParent> set = new ObjectOpenHashSet<>();
        populateNodeAndItsParent(df, set);
        int count = 0;
        while (attemptDeduplication(set)) {
            count ++;
        }
        stopwatch.stop();
        System.out.println(String.format("Deduplication finished after %d iterations after %s", count, stopwatch));
    }

    private static void populateNodeAndItsParent(DensityFunction current, ObjectSet<NodeAndItsParent> set) {
        if (current instanceof IDensityFunction<?> idf) {
            final DensityFunction[] children = idf.dfuncopto$getChildren();
            for (DensityFunction child : children) {
                set.add(new NodeAndItsParent(child, idf));
                populateNodeAndItsParent(child, set);
            }
        }
    }

    private static boolean attemptDeduplication(ObjectSet<NodeAndItsParent> nodes) {
        // all nodes here have the same distance to leaf
        for (NodeAndItsParent node1 : nodes) {
            for (NodeAndItsParent node2 : nodes) {
                if (node1 == node2) continue;
                if (node1.node == node2.node) continue;
                if (node1.node.equals(node2.node)) {
                    // node1 and node2 are the same node
                    // we can replace node2 with node1
                    node2.parent.dfuncopto$replace(node2.node, node1.node);
                    // remove node2.parent -> node2 mapping from the current set
                    nodes.remove(node2);
                    // add node2.parent -> node1 mapping to the current set
                    nodes.add(new NodeAndItsParent(node1.node, node2.parent));
                    // restart the iteration
                    return true;
                }
            }
        }
        return false;
    }

    private record NodeAndItsParent(DensityFunction node, IDensityFunction<?> parent) {

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            NodeAndItsParent that = (NodeAndItsParent) o;
            return node == that.node && parent == that.parent;
        }

        @Override
        public int hashCode() {
            return Objects.hash(System.identityHashCode(node), System.identityHashCode(parent));
        }
    }

}
