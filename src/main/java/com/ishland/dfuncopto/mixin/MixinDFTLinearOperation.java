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

@Mixin(DensityFunctionTypes.LinearOperation.class)
public class MixinDFTLinearOperation implements IDensityFunction<DensityFunctionTypes.LinearOperation> {
    @Shadow @Final private DensityFunctionTypes.LinearOperation.SpecificType specificType;

    @Mutable
    @Shadow @Final private DensityFunction input;

    @Shadow @Final private double minValue;

    @Shadow @Final private double maxValue;

    @Shadow @Final private double argument;

    @Override
    public DensityFunctionTypes.LinearOperation dfuncopto$deepClone() {
        return new DensityFunctionTypes.LinearOperation(this.specificType, DensityFunctionUtil.deepClone(this.input), this.minValue, this.maxValue, this.argument);
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
}
