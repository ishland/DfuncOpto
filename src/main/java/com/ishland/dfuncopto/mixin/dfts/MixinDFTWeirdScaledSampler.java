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

@Mixin(DensityFunctionTypes.WeirdScaledSampler.class)
public class MixinDFTWeirdScaledSampler implements IDensityFunction<DensityFunctionTypes.WeirdScaledSampler> {

    @Mutable
    @Shadow @Final private DensityFunction input;

    @Shadow @Final private DensityFunction.Noise noise;

    @Shadow @Final private DensityFunctionTypes.WeirdScaledSampler.RarityValueMapper rarityValueMapper;

    @Override
    public DensityFunctionTypes.WeirdScaledSampler dfuncopto$deepClone0(Reference2ReferenceMap<DensityFunction, DensityFunction> cloneCache) {
        return new DensityFunctionTypes.WeirdScaledSampler(DensityFunctionUtil.deepClone(this.input, cloneCache), this.noise, this.rarityValueMapper);
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
        final DensityFunction apply1 = this.input.apply(visitor);
        final DensityFunction.Noise apply2 = visitor.apply(this.noise);
        if (apply1 == this.input && apply2 == this.noise) {
            return visitor.apply((DensityFunction) this);
        }
        return visitor.apply(new DensityFunctionTypes.WeirdScaledSampler(apply1, apply2, this.rarityValueMapper));
    }
}
