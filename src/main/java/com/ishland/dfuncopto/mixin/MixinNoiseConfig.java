package com.ishland.dfuncopto.mixin;

import com.ishland.dfuncopto.common.OptoTransformation;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.noise.NoiseRouter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NoiseConfig.class)
public class MixinNoiseConfig {

    @Mutable
    @Shadow @Final private NoiseRouter noiseRouter;

    @Mutable
    @Shadow @Final private MultiNoiseUtil.MultiNoiseSampler multiNoiseSampler;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void postInit(CallbackInfo ci) {
        this.noiseRouter = new NoiseRouter(
                OptoTransformation.copyAndOptimize(this.noiseRouter.barrierNoise()),
                this.noiseRouter.fluidLevelFloodednessNoise(),
                this.noiseRouter.fluidLevelSpreadNoise(),
                this.noiseRouter.lavaNoise(),
                this.noiseRouter.temperature(),
                this.noiseRouter.vegetation(),
                this.noiseRouter.continents(),
                this.noiseRouter.erosion(),
                this.noiseRouter.depth(),
                this.noiseRouter.ridges(),
                this.noiseRouter.initialDensityWithoutJaggedness(),
                OptoTransformation.copyAndOptimize(this.noiseRouter.finalDensity()),
                this.noiseRouter.veinToggle(),
                this.noiseRouter.veinRidged(),
                this.noiseRouter.veinGap()
        );

        this.multiNoiseSampler = new MultiNoiseUtil.MultiNoiseSampler(
                this.multiNoiseSampler.temperature(),
                this.multiNoiseSampler.humidity(),
                this.multiNoiseSampler.continentalness(),
                this.multiNoiseSampler.erosion(),
                this.multiNoiseSampler.depth(),
                this.multiNoiseSampler.weirdness(),
                this.multiNoiseSampler.spawnTarget()
        );
    }

}
