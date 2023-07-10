package com.ishland.dfuncopto.mixin.dfts;

import com.ishland.dfuncopto.common.IDensityFunction;
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
        return new DensityFunctionTypes.RegistryEntryHolder(this.function);
    }

    @Override
    public void dfuncopto$replace(DensityFunction original, DensityFunction replacement) {
        if (this.function == original) {
            this.function = new RegistryEntry.Direct<>(replacement);
        } else {
            throw new IllegalStateException("Cannot replace non-child node!");
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
     * @reason Reduce object allocation & shorten call chain
     */
    @Overwrite
    public DensityFunction apply(DensityFunction.DensityFunctionVisitor visitor) {
        final DensityFunction apply = this.function.value().apply(visitor);
        if (apply == this.function.value()) {
            final DensityFunction apply1 = visitor.apply((DensityFunction) this);
            if (apply1 == (Object) this) return this.function.value();
            return apply1;
        } else {
            final DensityFunctionTypes.RegistryEntryHolder newf = new DensityFunctionTypes.RegistryEntryHolder(new RegistryEntry.Direct<>(apply));
            final DensityFunction apply1 = visitor.apply(newf);
            if (apply1 == newf) return apply;
            return apply1;
        }
    }
}
