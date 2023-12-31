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

@Mixin(DensityFunctionTypes.ShiftedNoise.class)
public class MixinDFTShiftedNoise implements IDensityFunction<DensityFunctionTypes.ShiftedNoise> {


    @Mutable
    @Shadow @Final private DensityFunction shiftX;

    @Mutable
    @Shadow @Final private DensityFunction shiftY;

    @Mutable
    @Shadow @Final private DensityFunction shiftZ;

    @Shadow @Final private double xzScale;

    @Shadow @Final private double yScale;

    @Shadow @Final private DensityFunction.Noise noise;

    @Override
    public DensityFunctionTypes.ShiftedNoise dfuncopto$deepClone0(Reference2ReferenceMap<DensityFunction, DensityFunction> cloneCache) {
        return new DensityFunctionTypes.ShiftedNoise(
                DensityFunctionUtil.deepClone(this.shiftX, cloneCache),
                DensityFunctionUtil.deepClone(this.shiftY, cloneCache),
                DensityFunctionUtil.deepClone(this.shiftZ, cloneCache),
                this.xzScale,
                this.yScale,
                noise
        );
    }

    @Override
    public void dfuncopto$replace(DensityFunction original, DensityFunction replacement) {
        boolean hasReplaced = false;
        if (this.shiftX == original) {
            this.shiftX = replacement;
            hasReplaced = true;
        }
        if (this.shiftY == original) {
            this.shiftY = replacement;
            hasReplaced = true;
        }
        if (this.shiftZ == original) {
            this.shiftZ = replacement;
            hasReplaced = true;
        }
        if (!hasReplaced) throw new IllegalArgumentException(SharedConstants.INVALID_ORIGINAL_DFUNC);
    }

    @Override
    public DensityFunction[] dfuncopto$getChildren() {
        return new DensityFunction[] {
                this.shiftX,
                this.shiftY,
                this.shiftZ
        };
    }

    /**
     * @author ishland
     * @reason Reduce object allocation
     */
    @Overwrite
    public DensityFunction apply(DensityFunction.DensityFunctionVisitor visitor) {
        final DensityFunction apply1 = this.shiftX.apply(visitor);
        final DensityFunction apply2 = this.shiftY.apply(visitor);
        final DensityFunction apply3 = this.shiftZ.apply(visitor);
        final DensityFunction.Noise apply4 = visitor.apply(this.noise);
        if (apply1 == this.shiftX && apply2 == this.shiftY && apply3 == this.shiftZ && apply4 == this.noise) {
            return visitor.apply((DensityFunction) this);
        }
        return visitor.apply(
                new DensityFunctionTypes.ShiftedNoise(
                        apply1, apply2, apply3, this.xzScale, this.yScale, apply4
                )
        );
    }
}
