package com.ishland.dfuncopto.common;

import net.minecraft.world.gen.densityfunction.DensityFunction;

public interface IDensityFunction<T extends DensityFunction> {

    T dfuncopto$deepClone();

    void dfuncopto$replace(DensityFunction original, DensityFunction replacement);

    DensityFunction[] dfuncopto$getChildren();

}
