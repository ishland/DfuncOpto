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

@Mixin(DensityFunctionTypes.RangeChoice.class)
public class MixinDFTRangeChoice implements IDensityFunction<DensityFunctionTypes.RangeChoice> {
    @Mutable
    @Shadow @Final private DensityFunction input;

    @Shadow @Final private double minInclusive;

    @Shadow @Final private double maxExclusive;

    @Mutable
    @Shadow @Final private DensityFunction whenInRange;

    @Mutable
    @Shadow @Final private DensityFunction whenOutOfRange;

    @Override
    public DensityFunctionTypes.RangeChoice dfuncopto$deepClone0(Reference2ReferenceMap<DensityFunction, DensityFunction> cloneCache) {
        return new DensityFunctionTypes.RangeChoice(
                DensityFunctionUtil.deepClone(this.input, cloneCache),
                this.minInclusive,
                this.maxExclusive,
                DensityFunctionUtil.deepClone(this.whenInRange, cloneCache),
                DensityFunctionUtil.deepClone(this.whenOutOfRange, cloneCache)
        );
    }

    @Override
    public void dfuncopto$replace(DensityFunction original, DensityFunction replacement) {
        boolean hasReplaced = false;
        if (this.input == original) {
            this.input = replacement;
            hasReplaced = true;
        }
        if (this.whenInRange == original) {
            this.whenInRange = replacement;
            hasReplaced = true;
        }
        if (this.whenOutOfRange == original) {
            this.whenOutOfRange = replacement;
            hasReplaced = true;
        }
        if (!hasReplaced) throw new IllegalArgumentException(SharedConstants.INVALID_ORIGINAL_DFUNC);
    }

    @Override
    public DensityFunction[] dfuncopto$getChildren() {
        return new DensityFunction[] {
                this.input,
                this.whenInRange,
                this.whenOutOfRange
        };
    }

    /**
     * @author ishland
     * @reason Reduce object allocation
     */
    @Overwrite
    public DensityFunction apply(DensityFunction.DensityFunctionVisitor visitor) {
        final DensityFunction apply = this.input.apply(visitor);
        final DensityFunction apply2 = this.whenInRange.apply(visitor);
        final DensityFunction apply3 = this.whenOutOfRange.apply(visitor);
        if (apply == this.input && apply2 == this.whenInRange && apply3 == this.whenOutOfRange) return visitor.apply((DensityFunction) this);
        return visitor.apply(new DensityFunctionTypes.RangeChoice(apply, this.minInclusive, this.maxExclusive, apply2, apply3));
    }
}
