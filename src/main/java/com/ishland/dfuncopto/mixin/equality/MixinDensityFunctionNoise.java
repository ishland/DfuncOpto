package com.ishland.dfuncopto.mixin.equality;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Objects;

@Mixin(DensityFunction.Noise.class)
public class MixinDensityFunctionNoise {

    @Shadow @Final private @Nullable DoublePerlinNoiseSampler noise;

    @Shadow @Final private RegistryEntry<DoublePerlinNoiseSampler.NoiseParameters> noiseData;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MixinDensityFunctionNoise that = (MixinDensityFunctionNoise) o;
        return Objects.equals(noise, that.noise) && Objects.equals(this.noiseData.value(), this.noiseData.value());
    }

    @Override
    public int hashCode() {
        return Objects.hash(noise, noiseData.value());
    }
}
