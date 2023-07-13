package com.ishland.dfuncopto.mixin.dfts;

import com.ishland.dfuncopto.common.DensityFunctionUtil;
import com.ishland.dfuncopto.common.IDensityFunction;
import com.ishland.dfuncopto.common.SharedConstants;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DensityFunctionTypes.LinearOperation.class)
public abstract class MixinDFTLinearOperation implements IDensityFunction<DensityFunctionTypes.LinearOperation>, DensityFunctionTypes.Unary, DensityFunctionTypes.BinaryOperationLike {
    @Shadow @Final private DensityFunctionTypes.LinearOperation.SpecificType specificType;

    @Mutable
    @Shadow @Final private DensityFunction input;

    @Shadow @Final private double minValue;

    @Shadow @Final private double maxValue;

    @Shadow @Final private double argument;

    @Shadow public abstract Type type();

    @Override
    public DensityFunctionTypes.LinearOperation dfuncopto$deepClone0(Reference2ReferenceMap<DensityFunction, DensityFunction> cloneCache) {
        final DensityFunctionTypes.LinearOperation copy = new DensityFunctionTypes.LinearOperation(this.specificType, DensityFunctionUtil.deepClone(this.input, cloneCache), this.minValue, this.maxValue, this.argument);
        return copy;
    }

    private boolean dfuncopto$isMinMaxDirty = false;

    @Override
    public void dfuncopto$replace(DensityFunction original, DensityFunction replacement) {
        if (this.input == original) {
            this.input = replacement;
        } else {
            throw new IllegalArgumentException(SharedConstants.INVALID_ORIGINAL_DFUNC);
        }
        if (original.minValue() != replacement.minValue() && original.maxValue() != replacement.maxValue()) {
            this.dfuncopto$isMinMaxDirty = true;
        }
    }

    @Override
    public DensityFunction[] dfuncopto$getChildren() {
        return new DensityFunction[] {
                this.input
        };
    }

    /**
     * @author ishland
     * @reason Reduce object allocation
     */
    @Overwrite
    public DensityFunction apply(DensityFunction.DensityFunctionVisitor visitor) {
        DensityFunction densityFunction = this.input.apply(visitor);
        if (!dfuncopto$isMinMaxDirty && densityFunction == this.input) {
            return visitor.apply((DensityFunction) this);
        }

        double newMin;
        double newMax;
        if (this.specificType == DensityFunctionTypes.LinearOperation.SpecificType.ADD) {
            newMin = densityFunction.minValue() + this.argument;
            newMax = densityFunction.maxValue() + this.argument;
        } else if (this.argument >= 0.0) {
            newMin = densityFunction.minValue() * this.argument;
            newMax = densityFunction.maxValue() * this.argument;
        } else {
            newMin = densityFunction.maxValue() * this.argument;
            newMax = densityFunction.minValue() * this.argument;
        }

        return new DensityFunctionTypes.LinearOperation(this.specificType, densityFunction, newMin, newMax, this.argument);
    }

    @Override
    public void fill(double[] densities, DensityFunction.EachApplier applier) {
        this.input.fill(densities, applier);
        switch (this.specificType) {
            case MUL -> {
                for (int i = 0, densitiesLength = densities.length; i < densitiesLength; i++) {
                    densities[i] *= this.argument;
                }
            }
            case ADD -> {
                for (int i = 0, densitiesLength = densities.length; i < densitiesLength; i++) {
                    densities[i] += this.argument;
                }
            }
        }
    }
}
