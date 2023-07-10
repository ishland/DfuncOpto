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
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DensityFunctionTypes.Wrapping.class)
public abstract class MixinDFTWrapping implements IDensityFunction<DensityFunctionTypes.Wrapping>, DensityFunctionTypes.Wrapper {
    @Shadow @Final private DensityFunctionTypes.Wrapping.Type type;

    @Mutable
    @Shadow @Final private DensityFunction wrapped;

    @Override
    public DensityFunctionTypes.Wrapping dfuncopto$deepClone0(Reference2ReferenceMap<DensityFunction, DensityFunction> cloneCache) {
        return new DensityFunctionTypes.Wrapping(this.type, DensityFunctionUtil.deepClone(this.wrapped, cloneCache));
    }

    @Override
    public void dfuncopto$replace(DensityFunction original, DensityFunction replacement) {
        if (this.wrapped == original) {
            this.wrapped = replacement;
        } else {
            throw new IllegalArgumentException(SharedConstants.INVALID_ORIGINAL_DFUNC);
        }
    }

    @Override
    public DensityFunction[] dfuncopto$getChildren() {
        return new DensityFunction[] {
                this.wrapped
        };
    }

    @Override
    public DensityFunction apply(DensityFunction.DensityFunctionVisitor visitor) {
        final DensityFunction apply = this.wrapped.apply(visitor);
        if (apply == this.wrapped) {
            return visitor.apply((DensityFunction) this);
        }
        return visitor.apply(new DensityFunctionTypes.Wrapping(this.type, apply));
    }
}
