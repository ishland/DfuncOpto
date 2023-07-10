package com.ishland.dfuncopto.mixin.dfts;

import com.ishland.dfuncopto.common.DensityFunctionUtil;
import com.ishland.dfuncopto.common.IDensityFunction;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DensityFunctionTypes.BinaryOperation.class)
public class MixinDFTBinaryOperation implements IDensityFunction<DensityFunctionTypes.BinaryOperation> {
    @Mutable
    @Shadow @Final private DensityFunction argument1;

    @Mutable
    @Shadow @Final private DensityFunction argument2;

    @Shadow @Final private DensityFunctionTypes.BinaryOperationLike.Type type;

    @Shadow @Final private double minValue;

    @Shadow @Final private double maxValue;

    @Override
    public DensityFunctionTypes.BinaryOperation dfuncopto$deepClone0(Reference2ReferenceMap<DensityFunction, DensityFunction> cloneCache) {
        return new DensityFunctionTypes.BinaryOperation(
                this.type,
                DensityFunctionUtil.deepClone(this.argument1, cloneCache),
                DensityFunctionUtil.deepClone(this.argument2, cloneCache),
                this.minValue,
                this.maxValue
        );
    }

    @Override
    public void dfuncopto$replace(DensityFunction original, DensityFunction replacement) {
        if (this.argument1 == original) {
            this.argument1 = replacement;
        } else if (this.argument2 == original) {
            this.argument2 = replacement;
        } else {
            throw new IllegalStateException("Cannot replace non-child node!");
        }
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
