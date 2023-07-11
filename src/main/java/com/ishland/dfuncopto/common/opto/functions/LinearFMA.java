package com.ishland.dfuncopto.common.opto.functions;

import com.ishland.dfuncopto.common.DensityFunctionUtil;
import com.ishland.dfuncopto.common.IDensityFunction;
import com.ishland.dfuncopto.common.SharedConstants;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import net.minecraft.util.dynamic.CodecHolder;
import net.minecraft.world.gen.densityfunction.DensityFunction;

import java.util.Objects;

public final class LinearFMA implements IDensityFunction<LinearFMA>, DensityFunction {
    private DensityFunction input;
    private final double minValue;
    private final double maxValue;
    private final double mul;
    private final double add;

    public LinearFMA(DensityFunction input, double mul, double add) {
        this.input = input;
        this.mul = mul;
        this.add = add;
        this.minValue = input.minValue() * mul + add;
        this.maxValue = input.maxValue() * mul + add;
    }

    @Override
    public double sample(NoisePos pos) {
        if (SharedConstants.hasFMA) {
            return Math.fma(input.sample(pos), mul, add);
        } else {
            return input.sample(pos) * mul + add;
        }
    }

    @Override
    public void fill(double[] densities, EachApplier applier) {
        input.fill(densities, applier);
        if (SharedConstants.hasFMA) {
            for (int i = 0; i < densities.length; i++) {
                densities[i] = Math.fma(densities[i], mul, add);
            }
        } else {
            for (int i = 0; i < densities.length; i++) {
                densities[i] = densities[i] * mul + add;
            }
        }
    }

    @Override
    public DensityFunction apply(DensityFunctionVisitor visitor) {
        final DensityFunction apply = this.input.apply(visitor);
        if (apply == this.input) return visitor.apply(this);
        // recalculate min max
        return visitor.apply(new LinearFMA(apply, mul, add));
    }

    @Override
    public CodecHolder<? extends DensityFunction> getCodecHolder() {
        throw new UnsupportedOperationException();
    }

    @Override
    public LinearFMA dfuncopto$deepClone0(Reference2ReferenceMap<DensityFunction, DensityFunction> cloneCache) {
        return new LinearFMA(DensityFunctionUtil.deepClone(input, cloneCache), mul, add);
    }

    @Override
    public void dfuncopto$replace(DensityFunction original, DensityFunction replacement) {
        if (input == original) {
            input = replacement;
        } else {
            throw new IllegalArgumentException("Original density function is not a child of this density function");
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
