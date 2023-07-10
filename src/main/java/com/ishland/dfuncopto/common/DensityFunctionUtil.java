package com.ishland.dfuncopto.common;

import com.google.common.collect.ImmutableList;
import com.ishland.dfuncopto.mixin.access.ISplineImplementation;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.util.function.ToFloatFunction;
import net.minecraft.util.math.Spline;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class DensityFunctionUtil {

    public static DensityFunction deepClone(DensityFunction df, Reference2ReferenceMap<DensityFunction, DensityFunction> cloneCache) {
        if (df instanceof IDensityFunction<?> iDensityFunction) {
            return iDensityFunction.dfuncopto$deepClone(cloneCache);
        }
        return df;
    }

    public static <C, I extends ToFloatFunction<C>> Spline<C, I> deepClone(Spline<C, I> spline, Reference2ReferenceMap<DensityFunction, DensityFunction> cloneCache) {
        if (spline instanceof Spline.Implementation<C,I> implementation) {
            I locationFunction = implementation.locationFunction() instanceof DensityFunction df ? (I) deepClone(df, cloneCache) : implementation.locationFunction();
            List<Spline<C, I>> values = implementation.values().stream().map(spline1 -> deepClone(spline1, cloneCache)).toList();
            return new Spline.Implementation<>(
                    locationFunction,
                    implementation.locations().clone(),
                    ImmutableList.copyOf(values),
                    implementation.derivatives().clone(),
                    implementation.min(),
                    implementation.max()
            );
        }
        return spline;
    }

    public static <C, I extends ToFloatFunction<C>> void acceptDensityFunctions(Spline<C, I> spline, Consumer<DensityFunction> function) {
        acceptDensityFunctions(spline, df -> {
            function.accept(df);
            return null;
        });
    }

    public static <C, I extends ToFloatFunction<C>> boolean acceptDensityFunctions(Spline<C, I> spline, Function<DensityFunction, DensityFunction> function) {
        if (spline instanceof Spline.Implementation<C,I> implementation) {
            boolean modified = false;
            if (implementation.locationFunction() instanceof DensityFunctionTypes.Spline.DensityFunctionWrapper wrapper) {
                final DensityFunction apply = Objects.requireNonNullElse(function.apply(wrapper.function().value()), wrapper.function().value());
                if (apply != wrapper.function().value()) {
                    ((ISplineImplementation<C, I>) (Object) implementation).setLocationFunction((I) apply);
                    modified = true;
                }
            }
            for (Spline<C, I> value : implementation.values()) {
                modified |= acceptDensityFunctions(value, function);
            }
            return modified;
        }
        return false;
    }

}
