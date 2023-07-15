package com.ishland.dfuncopto.common.opto.functions;

import net.minecraft.util.dynamic.CodecHolder;
import net.minecraft.world.gen.densityfunction.DensityFunction;

public class Cache3D implements DensityFunction {

    private final DensityFunction wrapped;
    private int lastX = Integer.MAX_VALUE;
    private int lastY = Integer.MAX_VALUE;
    private int lastZ = Integer.MAX_VALUE;
    private double lastValue = Double.POSITIVE_INFINITY;

    public Cache3D(DensityFunction wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public double sample(NoisePos pos) {
        if (pos.blockX() == this.lastX && pos.blockY() == this.lastY && pos.blockZ() == this.lastZ) {
            return this.lastValue;
        }
        this.lastX = pos.blockX();
        this.lastY = pos.blockY();
        this.lastZ = pos.blockZ();
        return this.lastValue = this.wrapped.sample(pos);
    }

    @Override
    public void fill(double[] densities, EachApplier applier) {
        this.wrapped.fill(densities, applier);
    }

    @Override
    public DensityFunction apply(DensityFunctionVisitor visitor) {
        final DensityFunction apply = this.wrapped.apply(visitor);
        if (apply == this.wrapped) return visitor.apply(this);
        return visitor.apply(new Cache3D(apply));
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
        throw new UnsupportedOperationException();
    }
}
