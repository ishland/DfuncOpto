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

@Mixin(DensityFunctionTypes.UnaryOperation.class)
public class MixinDFTUnaryOperation implements IDensityFunction<DensityFunctionTypes.UnaryOperation> {

    @Shadow @Final private DensityFunctionTypes.UnaryOperation.Type type;

    @Mutable
    @Shadow @Final private DensityFunction input;

    @Shadow @Final private double minValue;

    @Shadow @Final private double maxValue;

    @Override
    public DensityFunctionTypes.UnaryOperation dfuncopto$deepClone0(Reference2ReferenceMap<DensityFunction, DensityFunction> cloneCache) {
        return new DensityFunctionTypes.UnaryOperation(this.type, DensityFunctionUtil.deepClone(this.input, cloneCache), this.minValue, this.maxValue);
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
}
