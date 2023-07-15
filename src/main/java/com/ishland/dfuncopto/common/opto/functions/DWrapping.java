package com.ishland.dfuncopto.common.opto.functions;

import net.minecraft.util.dynamic.CodecHolder;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;

public record DWrapping(Type type, DensityFunction wrapped) implements DensityFunction {

    @Override
    public double sample(DensityFunction.NoisePos pos) {
        return this.wrapped.sample(pos);
    }

    @Override
    public void fill(double[] densities, DensityFunction.EachApplier applier) {
        this.wrapped.fill(densities, applier);
    }

    @Override
    public DensityFunction apply(DensityFunctionVisitor visitor) {
        final DensityFunction apply = this.wrapped.apply(visitor);
        if (apply == this.wrapped) return visitor.apply(this);
        return visitor.apply(new DWrapping(this.type, apply));
    }

    @Override
    public double minValue() {
        return this.wrapped.minValue();
    }

    @Override
    public double maxValue() {
        return this.wrapped.maxValue();
    }

    @Override
    public CodecHolder<? extends DensityFunction> getCodecHolder() {
        return null;
    }

    public enum Type {
        Cache3D,
        Cache2D
    }

}
