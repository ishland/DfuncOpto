package com.ishland.dfuncopto.mixin.dfts;

import com.ishland.dfuncopto.common.DFCacheControl;
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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DensityFunctionTypes.LinearOperation.class)
public abstract class MixinDFTLinearOperation implements IDensityFunction<DensityFunctionTypes.LinearOperation>, DFCacheControl, DensityFunctionTypes.Unary, DensityFunctionTypes.BinaryOperationLike {
    @Shadow @Final private DensityFunctionTypes.LinearOperation.SpecificType specificType;

    @Mutable
    @Shadow @Final private DensityFunction input;

    @Mutable
    @Shadow @Final private double minValue;

    @Mutable
    @Shadow @Final private double maxValue;

    @Shadow @Final private double argument;

    @Shadow public abstract Type type();

    @Override
    public DensityFunctionTypes.LinearOperation dfuncopto$deepClone0(Reference2ReferenceMap<DensityFunction, DensityFunction> cloneCache) {
        final DensityFunctionTypes.LinearOperation copy = new DensityFunctionTypes.LinearOperation(this.specificType, DensityFunctionUtil.deepClone(this.input, cloneCache), this.minValue, this.maxValue, this.argument);
//        ((DFCacheControl) (Object) copy).dfuncopto$refreshMinMaxCache();
        return copy;
    }

    private boolean dfuncopto$cacheDisabled = false;

    @Inject(method = {"minValue", "maxValue"}, at = @At("HEAD"))
    private void dfuncopto$beforeReadMinMax(CallbackInfoReturnable<Double> cir) {
        if (!dfuncopto$cacheDisabled) return;
        dfuncopto$refreshMinMaxCache();
    }

    @Override
    public void dfuncopto$setMinMaxCachingDisabled(boolean disabled) {
        this.dfuncopto$cacheDisabled = disabled;
    }

    @Override
    public void dfuncopto$refreshMinMaxCache() {
        final double inputMin = this.input.minValue();
        final double inputMax = this.input.maxValue();
        double min;
        double max;
        if (this.specificType == DensityFunctionTypes.LinearOperation.SpecificType.ADD) {
            min = inputMin + this.argument;
            max = inputMax + this.argument;
        } else if (this.argument >= 0.0) {
            min = inputMin * this.argument;
            max = inputMax * this.argument;
        } else {
            min = inputMax * this.argument;
            max = inputMin * this.argument;
        }
        this.minValue = min;
        this.maxValue = max;
    }

    @Override
    public void dfuncopto$replace(DensityFunction original, DensityFunction replacement) {
        if (this.input == original) {
            this.input = replacement;
        } else {
            throw new IllegalArgumentException(SharedConstants.INVALID_ORIGINAL_DFUNC);
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
        if (densityFunction == this.input) {
            return visitor.apply((DensityFunction) this);
        }

        final DensityFunctionTypes.LinearOperation operation = new DensityFunctionTypes.LinearOperation(this.specificType, densityFunction, 0, 0, this.argument);
        ((DFCacheControl) (Object) operation).dfuncopto$refreshMinMaxCache();
        return operation;
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
