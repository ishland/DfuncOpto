package com.ishland.dfuncopto.mixin;

import com.ishland.dfuncopto.common.DensityFunctionUtil;
import com.ishland.dfuncopto.common.IDensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DensityFunctionTypes.Clamp.class)
public class MixinDFTClamp implements IDensityFunction<DensityFunctionTypes.Clamp> {

    @Mutable
    @Shadow @Final private DensityFunction input;

    @Shadow @Final private double minValue;

    @Shadow @Final private double maxValue;

    @Override
    public DensityFunctionTypes.Clamp dfuncopto$deepClone() {
        return new DensityFunctionTypes.Clamp(DensityFunctionUtil.deepClone(this.input), this.minValue, this.maxValue);
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
        final DensityFunction apply = this.input.apply(visitor);
        if (apply == this.input) return visitor.apply((DensityFunction) this);
        return new DensityFunctionTypes.Clamp(apply, this.minValue, this.maxValue);
    }
}
