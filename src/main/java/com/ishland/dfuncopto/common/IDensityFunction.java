package com.ishland.dfuncopto.common;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import net.minecraft.world.gen.densityfunction.DensityFunction;

public interface IDensityFunction<T extends DensityFunction> {

    default T dfuncopto$deepClone(Reference2ReferenceMap<DensityFunction, DensityFunction> cloneCache) {
        if (cloneCache.containsKey((DensityFunction) this)) {
            return (T) cloneCache.get(this);
        }
        final T clone = dfuncopto$deepClone0(cloneCache);
        // the above method isn't side effect free, so re-check cache
        if (cloneCache.containsKey((DensityFunction) this)) {
            return (T) cloneCache.get(this);
        }
        cloneCache.put((DensityFunction) this, clone);
        return clone;
    }

    T dfuncopto$deepClone0(Reference2ReferenceMap<DensityFunction, DensityFunction> cloneCache);

    void dfuncopto$replace(DensityFunction original, DensityFunction replacement);

    DensityFunction[] dfuncopto$getChildren();

}
