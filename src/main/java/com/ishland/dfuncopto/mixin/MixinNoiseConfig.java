package com.ishland.dfuncopto.mixin;

import com.ishland.dfuncopto.common.OptoTransformation;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.noise.NoiseRouter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = NoiseConfig.class, priority = 950)
public class MixinNoiseConfig {

    @Mutable
    @Shadow @Final private NoiseRouter noiseRouter;

    @Mutable
    @Shadow @Final private MultiNoiseUtil.MultiNoiseSampler multiNoiseSampler;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void postInit(CallbackInfo ci) {
        this.noiseRouter = new NoiseRouter(
                OptoTransformation.copyAndOptimize("barrierNoise", this.noiseRouter.barrierNoise()),
                OptoTransformation.copyAndOptimize("fluidLevelFloodednessNoise", this.noiseRouter.fluidLevelFloodednessNoise()),
                OptoTransformation.copyAndOptimize("fluidLevelSpreadNoise", this.noiseRouter.fluidLevelSpreadNoise()),
                OptoTransformation.copyAndOptimize("lavaNoise", this.noiseRouter.lavaNoise()),
                OptoTransformation.copyAndOptimize("temperature", this.noiseRouter.temperature()),
                OptoTransformation.copyAndOptimize("vegetation", this.noiseRouter.vegetation()),
                OptoTransformation.copyAndOptimize("continents", this.noiseRouter.continents()),
                OptoTransformation.copyAndOptimize("erosion", this.noiseRouter.erosion()),
                OptoTransformation.copyAndOptimize("depth", this.noiseRouter.depth()),
                OptoTransformation.copyAndOptimize("ridges", this.noiseRouter.ridges()),
                OptoTransformation.copyAndOptimize("initialDensityWithoutJaggedness", this.noiseRouter.initialDensityWithoutJaggedness()),
                OptoTransformation.copyAndOptimize("finalDensity", this.noiseRouter.finalDensity()),
                OptoTransformation.copyAndOptimize("veinToggle", this.noiseRouter.veinToggle()),
                OptoTransformation.copyAndOptimize("veinRidged", this.noiseRouter.veinRidged()),
                OptoTransformation.copyAndOptimize("veinGap", this.noiseRouter.veinGap())
        );

        this.multiNoiseSampler = new MultiNoiseUtil.MultiNoiseSampler(
                OptoTransformation.copyAndOptimize("unwrapped temperature", this.multiNoiseSampler.temperature()),
                OptoTransformation.copyAndOptimize("unwrapped humidity", this.multiNoiseSampler.humidity()),
                OptoTransformation.copyAndOptimize("unwrapped continentalness", this.multiNoiseSampler.continentalness()),
                OptoTransformation.copyAndOptimize("unwrapped erosion", this.multiNoiseSampler.erosion()),
                OptoTransformation.copyAndOptimize("unwrapped depth", this.multiNoiseSampler.depth()),
                OptoTransformation.copyAndOptimize("unwrapped weirdness", this.multiNoiseSampler.weirdness()),
                this.multiNoiseSampler.spawnTarget()
        );
    }

}
