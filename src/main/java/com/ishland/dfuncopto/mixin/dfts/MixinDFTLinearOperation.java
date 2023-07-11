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
        final DensityFunctionTypes.BinaryOperationLike recalc = DensityFunctionTypes.BinaryOperationLike.create(this.type(), this.input, new DensityFunctionTypes.Constant(this.argument));
        this.minValue = recalc.minValue();
        this.maxValue = recalc.maxValue();
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
        double d = densityFunction.minValue();
        double e = densityFunction.maxValue();
        double f;
        double g;
        if (this.specificType == DensityFunctionTypes.LinearOperation.SpecificType.ADD) {
            f = d + this.argument;
            g = e + this.argument;
        } else if (this.argument >= 0.0) {
            f = d * this.argument;
            g = e * this.argument;
        } else {
            f = e * this.argument;
            g = d * this.argument;
        }

        return new DensityFunctionTypes.LinearOperation(this.specificType, densityFunction, f, g, this.argument);
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
