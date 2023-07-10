package com.ishland.dfuncopto.common.opto.functions;

import com.ishland.dfuncopto.common.DensityFunctionUtil;
import com.ishland.dfuncopto.common.IDensityFunction;
import net.minecraft.util.dynamic.CodecHolder;
import net.minecraft.world.gen.densityfunction.DensityFunction;

import java.util.Objects;

public final class LinearFMA implements IDensityFunction<LinearFMA>, DensityFunction {
    private DensityFunction input;
    private final double minValue;
    private final double maxValue;
    private final double mul;
    private final double add;

    public LinearFMA(DensityFunction input, double minValue, double maxValue, double mul, double add) {
        this.input = input;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.mul = mul;
        this.add = add;
    }

    @Override
    public double sample(NoisePos pos) {
        return Math.fma(input.sample(pos), mul, add);
    }

    @Override
    public void fill(double[] densities, EachApplier applier) {
        input.fill(densities, applier);
        for (int i = 0; i < densities.length; i++) {
            densities[i] = Math.fma(densities[i], mul, add);
        }
    }

    @Override
    public DensityFunction apply(DensityFunctionVisitor visitor) {
        final DensityFunction apply = this.input.apply(visitor);
        if (apply == this.input) return visitor.apply(this);
        // recalculate min max
        return visitor.apply(new LinearFMA(apply, Math.fma(apply.minValue(), mul, add), Math.fma(apply.maxValue(), mul, add), mul, add));
    }

    @Override
    public CodecHolder<? extends DensityFunction> getCodecHolder() {
        throw new UnsupportedOperationException();
    }

    @Override
    public LinearFMA dfuncopto$deepClone() {
        return new LinearFMA(DensityFunctionUtil.deepClone(input), minValue, maxValue, mul, add);
    }

    @Override
    public void dfuncopto$replace(DensityFunction original, DensityFunction replacement) {
        if (this.input == original) {
            this.input = replacement;
        } else {
            throw new IllegalStateException("Cannot replace non-child node!");
        }
    }

    @Override
    public DensityFunction[] dfuncopto$getChildren() {
        return new DensityFunction[]{
                this.input
        };
    }

    public DensityFunction input() {
        return input;
    }

    @Override
    public double minValue() {
        return minValue;
    }

    @Override
    public double maxValue() {
        return maxValue;
    }

    public double mul() {
        return mul;
    }

    public double add() {
        return add;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (LinearFMA) obj;
        return Objects.equals(this.input, that.input) &&
               Double.doubleToLongBits(this.minValue) == Double.doubleToLongBits(that.minValue) &&
               Double.doubleToLongBits(this.maxValue) == Double.doubleToLongBits(that.maxValue) &&
               Double.doubleToLongBits(this.mul) == Double.doubleToLongBits(that.mul) &&
               Double.doubleToLongBits(this.add) == Double.doubleToLongBits(that.add);
    }

    @Override
    public int hashCode() {
        return Objects.hash(input, minValue, maxValue, mul, add);
    }

    @Override
    public String toString() {
        return "LinearFMA[" +
               "input=" + input + ", " +
               "minValue=" + minValue + ", " +
               "maxValue=" + maxValue + ", " +
               "mul=" + mul + ", " +
               "add=" + add + ']';
    }

}
