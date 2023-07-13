package com.ishland.dfuncopto.mixin.dfts;

import com.ishland.dfuncopto.common.DensityFunctionUtil;
import com.ishland.dfuncopto.common.IDensityFunction;
import com.ishland.dfuncopto.common.SharedConstants;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DensityFunctionTypes.RegistryEntryHolder.class)
public class MixinDFTRegistryEntryHolder implements IDensityFunction<DensityFunctionTypes.RegistryEntryHolder> {
    @Mutable
    @Shadow @Final private RegistryEntry<DensityFunction> function;

    @Override
    public DensityFunctionTypes.RegistryEntryHolder dfuncopto$deepClone0(Reference2ReferenceMap<DensityFunction, DensityFunction> cloneCache) {
        return new DensityFunctionTypes.RegistryEntryHolder(new RegistryEntry.Direct<>(DensityFunctionUtil.deepClone(this.function.value(), cloneCache)));
    }

    @Override
    public void dfuncopto$replace(DensityFunction original, DensityFunction replacement) {
        if (this.function.value() == original) {
            this.function = new RegistryEntry.Direct<>(replacement);
        } else {
            throw new IllegalArgumentException(SharedConstants.INVALID_ORIGINAL_DFUNC);
        }
    }

    @Override
    public DensityFunction[] dfuncopto$getChildren() {
        return new DensityFunction[] {
                this.function.value()
        };
    }

    /**
     * @author ishland
     * @reason Reduce object allocation
     */
    @Overwrite
    public DensityFunction apply(DensityFunction.DensityFunctionVisitor visitor) {
        final DensityFunction apply = this.function.value().apply(visitor);
        if (apply == this.function.value()) return visitor.apply((DensityFunction) (Object) this);
        return new DensityFunctionTypes.RegistryEntryHolder(new RegistryEntry.Direct<>(apply));
    }
}
