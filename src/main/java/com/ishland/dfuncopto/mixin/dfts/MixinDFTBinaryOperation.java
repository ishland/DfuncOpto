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

@Mixin(DensityFunctionTypes.BinaryOperation.class)
public class MixinDFTBinaryOperation implements IDensityFunction<DensityFunctionTypes.BinaryOperation>, DFCacheControl {
    @Mutable
    @Shadow @Final private DensityFunction argument1;

    @Mutable
    @Shadow @Final private DensityFunction argument2;

    @Shadow @Final private DensityFunctionTypes.BinaryOperationLike.Type type;

    @Mutable
    @Shadow @Final private double minValue;

    @Mutable
    @Shadow @Final private double maxValue;

    @Override
    public DensityFunctionTypes.BinaryOperation dfuncopto$deepClone0(Reference2ReferenceMap<DensityFunction, DensityFunction> cloneCache) {
        final DensityFunctionTypes.BinaryOperation copy = new DensityFunctionTypes.BinaryOperation(
                this.type,
                DensityFunctionUtil.deepClone(this.argument1, cloneCache),
                DensityFunctionUtil.deepClone(this.argument2, cloneCache),
                this.minValue,
                this.maxValue
        );
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
        final DensityFunctionTypes.BinaryOperationLike recalc = DensityFunctionTypes.BinaryOperationLike.create(this.type, this.argument1, this.argument2);
        this.minValue = recalc.minValue();
        this.maxValue = recalc.maxValue();
    }

    @Override
    public void dfuncopto$replace(DensityFunction original, DensityFunction replacement) {
        boolean hasReplaced = false;
        if (this.argument1 == original) {
            this.argument1 = replacement;
            hasReplaced = true;
        }
        if (this.argument2 == original) {
            this.argument2 = replacement;
            hasReplaced = true;
        }
        if (!hasReplaced) throw new IllegalArgumentException(SharedConstants.INVALID_ORIGINAL_DFUNC);
    }

    @Override
    public DensityFunction[] dfuncopto$getChildren() {
        return new DensityFunction[] {
                this.argument1,
                this.argument2
        };
    }

    /**
     * @author ishland
     * @reason Reduce object allocation
     */
    @Overwrite
    public DensityFunction apply(DensityFunction.DensityFunctionVisitor visitor) {
        final DensityFunction apply1 = this.argument1.apply(visitor);
        final DensityFunction apply2 = this.argument2.apply(visitor);
        if (apply1 == this.argument1 && apply2 == this.argument2) return visitor.apply((DensityFunction) this);
        return visitor.apply(DensityFunctionTypes.BinaryOperationLike.create(this.type, apply1, apply2));
    }
}
