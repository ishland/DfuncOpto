package com.ishland.dfuncopto.mixin.dfts;

import com.ishland.dfuncopto.common.DFCacheControl;
import com.ishland.dfuncopto.common.DensityFunctionUtil;
import com.ishland.dfuncopto.common.IDensityFunction;
import com.ishland.dfuncopto.common.SharedConstants;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import net.minecraft.util.math.MathHelper;
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

@Mixin(DensityFunctionTypes.UnaryOperation.class)
public abstract class MixinDFTUnaryOperation implements IDensityFunction<DensityFunctionTypes.UnaryOperation>, DFCacheControl, DensityFunctionTypes.Unary {

    @Shadow @Final private DensityFunctionTypes.UnaryOperation.Type type;

    @Mutable
    @Shadow @Final private DensityFunction input;

    @Mutable
    @Shadow @Final private double minValue;

    @Mutable
    @Shadow @Final private double maxValue;

    @Override
    public DensityFunctionTypes.UnaryOperation dfuncopto$deepClone0(Reference2ReferenceMap<DensityFunction, DensityFunction> cloneCache) {
        final DensityFunctionTypes.UnaryOperation copy = new DensityFunctionTypes.UnaryOperation(this.type, DensityFunctionUtil.deepClone(this.input, cloneCache), this.minValue, this.maxValue);
        ((DFCacheControl) (Object) copy).dfuncopto$refreshMinMaxCache();
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
        final DensityFunctionTypes.UnaryOperation recalc = DensityFunctionTypes.UnaryOperation.create(this.type, this.input);
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
    public DensityFunctionTypes.UnaryOperation apply(DensityFunction.DensityFunctionVisitor visitor) {
        final DensityFunction apply = this.input.apply(visitor);

        // there is no visitor call for `this` in vanilla, but we call it anyway
        if (apply == this.input) {
            return (DensityFunctionTypes.UnaryOperation) visitor.apply((DensityFunction) this);
        }
        return (DensityFunctionTypes.UnaryOperation) visitor.apply(DensityFunctionTypes.UnaryOperation.create(this.type, apply));
    }

    @Override
    public void fill(double[] densities, EachApplier applier) {
        this.input.fill(densities, applier);
        switch (this.type) {
            case ABS -> {
                for (int i = 0, densitiesLength = densities.length; i < densitiesLength; i++) {
                    final double density = densities[i];
                    densities[i] = Math.abs(density);
                }
            }
            case SQUARE -> {
                for (int i = 0, densitiesLength = densities.length; i < densitiesLength; i++) {
                    final double density = densities[i];
                    densities[i] = density * density;
                }
            }
            case CUBE -> {
                for (int i = 0, densitiesLength = densities.length; i < densitiesLength; i++) {
                    final double density = densities[i];
                    densities[i] = density * density * density;
                }
            }
            case HALF_NEGATIVE -> {
                for (int i = 0, densitiesLength = densities.length; i < densitiesLength; i++) {
                    final double density = densities[i];
                    densities[i] = density > 0.0 ? density : density * 0.5;
                }
            }
            case QUARTER_NEGATIVE -> {
                for (int i = 0, densitiesLength = densities.length; i < densitiesLength; i++) {
                    final double density = densities[i];
                    densities[i] = density > 0.0 ? density : density * 0.25;
                }
            }
            case SQUEEZE -> {
                for (int i = 0, densitiesLength = densities.length; i < densitiesLength; i++) {
                    final double density = densities[i];
                    double d = MathHelper.clamp(density, -1.0, 1.0);
                    densities[i] = d / 2.0 - d * d * d / 24.0;
                }
            }
        }
    }
}
