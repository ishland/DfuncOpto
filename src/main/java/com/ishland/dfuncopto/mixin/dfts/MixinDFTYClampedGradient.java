package com.ishland.dfuncopto.mixin.dfts;

import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DensityFunctionTypes.YClampedGradient.class)
public abstract class MixinDFTYClampedGradient implements DensityFunction.Base {

    @Shadow @Final private int fromY;

    @Shadow @Final private int toY;

    @Shadow @Final private double fromValue;

    @Shadow @Final private double toValue;

    @Override
    public void fill(double[] densities, EachApplier applier) {
        final double fromYD = this.fromY;
        final double toYD = this.toY;
        for (int i = 0, densitiesLength = densities.length; i < densitiesLength; i++) {
            densities[i] = MathHelper.clampedMap(applier.at(i).blockY(), fromYD, toYD, this.fromValue, this.toValue);
        }
    }

}
