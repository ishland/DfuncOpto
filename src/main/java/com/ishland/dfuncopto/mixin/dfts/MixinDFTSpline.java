package com.ishland.dfuncopto.mixin.dfts;

import com.ishland.dfuncopto.common.DensityFunctionUtil;
import com.ishland.dfuncopto.common.IDensityFunction;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import net.minecraft.util.math.Spline;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.function.Consumer;

@Mixin(DensityFunctionTypes.Spline.class)
public class MixinDFTSpline implements IDensityFunction<DensityFunctionTypes.Spline> {
    @Shadow @Final private Spline<DensityFunctionTypes.Spline.SplinePos, DensityFunctionTypes.Spline.DensityFunctionWrapper> spline;

    @Override
    public DensityFunctionTypes.Spline dfuncopto$deepClone0(Reference2ReferenceMap<DensityFunction, DensityFunction> cloneCache) {
        return new DensityFunctionTypes.Spline(DensityFunctionUtil.deepClone(this.spline, cloneCache));
    }

    @Override
    public void dfuncopto$replace(DensityFunction original, DensityFunction replacement) {
        final boolean modified = DensityFunctionUtil.acceptDensityFunctions(this.spline, densityFunction -> {
            if (densityFunction == original) {
                return replacement;
            } else {
                return null;
            }
        });
        if (!modified) {
            throw new IllegalStateException("Cannot replace non-child node!");
        }
    }

    @Override
    public DensityFunction[] dfuncopto$getChildren() {
        ArrayList<DensityFunction> collector = new ArrayList<>();
        DensityFunctionUtil.acceptDensityFunctions(this.spline, (Consumer<DensityFunction>) collector::add);
        return collector.toArray(DensityFunction[]::new);
    }
}
