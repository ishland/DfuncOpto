package com.ishland.dfuncopto.common.test;

import com.google.common.base.Stopwatch;
import com.ishland.dfuncopto.common.OptoTransformation;
import com.ishland.dfuncopto.common.opto.functions.Cache3D;
import com.ishland.dfuncopto.common.opto.functions.DWrapping;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.ServerDynamicRegistryType;
import net.minecraft.resource.DataConfiguration;
import net.minecraft.resource.LifecycledResourceManager;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.VanillaDataPackProvider;
import net.minecraft.server.DataPackContents;
import net.minecraft.server.SaveLoading;
import net.minecraft.server.command.CommandManager;
import net.minecraft.util.Util;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.WorldPresets;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.noise.NoiseRouter;
import net.minecraft.world.level.WorldGenSettings;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class TestStandalone {

    private static DynamicRegistryManager.Immutable getRegistryManager() {
        record WorldCreationSettings(WorldGenSettings worldGenSettings, DataConfiguration dataConfiguration) {
        }

        ResourcePackManager resourcePackManager = VanillaDataPackProvider.createManager(FabricLoader.getInstance().getGameDir().resolve("benchmark-datapacks"));
        SaveLoading.DataPacks dataPacks = new SaveLoading.DataPacks(resourcePackManager, DataConfiguration.SAFE_MODE, false, true);
        SaveLoading.ServerConfig serverConfig = new SaveLoading.ServerConfig(dataPacks, CommandManager.RegistrationEnvironment.DEDICATED, 2);
        final ExecutorService service = Executors.newSingleThreadExecutor();
        CompletableFuture<GeneratorOptionsHolder> completableFuture = SaveLoading.load(
                serverConfig,
                context -> new SaveLoading.LoadContext<>(
                        new WorldCreationSettings(
                                new WorldGenSettings(GeneratorOptions.createRandom(), WorldPresets.createDemoOptions(context.worldGenRegistryManager())), context.dataConfiguration()
                        ),
                        context.dimensionsRegistryManager()
                ),
                // DO NOT CONVERT THIS TO LAMBDA DUE TO FREEZES
                new SaveLoading.SaveApplierFactory<WorldCreationSettings, GeneratorOptionsHolder>() {
                    @Override
                    public GeneratorOptionsHolder create(LifecycledResourceManager resourceManager, DataPackContents dataPackContents, CombinedDynamicRegistries<ServerDynamicRegistryType> combinedDynamicRegistries, WorldCreationSettings generatorOptions) {
                        resourceManager.close();
                        return new GeneratorOptionsHolder(generatorOptions.worldGenSettings(), combinedDynamicRegistries, dataPackContents, generatorOptions.dataConfiguration());
                    }
                },
                Util.getMainWorkerExecutor(),
                service
        );
        final GeneratorOptionsHolder holder = completableFuture.join();
        service.shutdown();
        return holder.getCombinedRegistryManager();
    }

    private static DensityFunction runApply(DensityFunction df, boolean enableExtraCaching) {
        return df.apply(densityFunction -> {
            if (densityFunction instanceof DensityFunctionTypes.Wrapping wrapping) {
                if (wrapping.type() == DensityFunctionTypes.Wrapping.Type.CACHE2D) {
                    return new ChunkNoiseSampler.Cache2D(wrapping.wrapped());
                }
            }
            if (enableExtraCaching) {
                if (densityFunction instanceof DWrapping wrapping) {
                    if (wrapping.type() == DWrapping.Type.Cache3D) {
                        return new Cache3D(wrapping.wrapped());
                    }
                    if (wrapping.type() == DWrapping.Type.Cache2D) {
                        return new ChunkNoiseSampler.Cache2D(wrapping.wrapped());
                    }
                }
            }

            return densityFunction;
        });
    }

    public static void main(String[] args) {
        SharedConstants.createGameVersion();
        Bootstrap.initialize();

        final DynamicRegistryManager.Immutable registryManager = getRegistryManager();
        final ChunkGeneratorSettings settings = registryManager.get(RegistryKeys.CHUNK_GENERATOR_SETTINGS).get(ChunkGeneratorSettings.OVERWORLD);
        OptoTransformation.DO_COMPILE = false;
        final NoiseRouter vanillaRouter = NoiseConfig.create(settings, registryManager.getWrapperOrThrow(RegistryKeys.NOISE_PARAMETERS), 0xffff).getNoiseRouter();
        final DensityFunction func = runApply(vanillaRouter.finalDensity(), false);
        OptoTransformation.DO_COMPILE = true;
        final NoiseRouter optimizedRouter = NoiseConfig.create(settings, registryManager.getWrapperOrThrow(RegistryKeys.NOISE_PARAMETERS), 0xffff).getNoiseRouter();
        final DensityFunction rfunc = runApply(optimizedRouter.finalDensity(), false);
        final DensityFunction rfuncd = runApply(optimizedRouter.finalDensity(), true);

        // horizontalCellCount: 4, verticalCellCount: 48

        for (int i = 0; i < 10; i++) {
            System.out.println("Iteration " + i);
            benchmark(rfunc, "DfuncOpto ");
            benchmark(rfuncd, "DfuncOptoD");
            benchmark(func, "Vanilla   ");
            benchmarkBatched(rfunc, "DfuncOpto ");
            benchmarkBatched(rfuncd, "DfuncOptoD");
            benchmarkBatched(func, "Vanilla   ");
            System.out.println();
        }
    }

    private static void benchmark(DensityFunction rfunc, String name) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        double sum = 0;
        for (int x = 0; x < 100; x++) {
            for (int z = 0; z < 100; z++) {
                for (int index = 0; index < 48; index ++) {
                    sum += rfunc.sample(new DensityFunction.UnblendedNoisePos(x << 2, -64 + (index * 8), z << 2));
                }
            }
        }
        stopwatch.stop();

        System.out.println(String.format("%s: Sum: %.10f, Time: %s", name, sum, stopwatch));
    }

    private static void benchmarkBatched(DensityFunction rfunc, String name) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        double sum = 0;
        double[] batch = new double[48];
        for (int x = 0; x < 100; x++) {
            for (int z = 0; z < 100; z++) {
                final BatchedZApplier applier = new BatchedZApplier(x << 2, z << 2);
                rfunc.fill(batch, applier);
                for (double v : batch) {
                    sum += v;
                }
            }
        }
        stopwatch.stop();

        System.out.println(String.format("%s batched: Sum: %.10f, Time: %s", name, sum, stopwatch));
    }

    private static class BatchedZApplier implements DensityFunction.EachApplier, DensityFunction.NoisePos {

        private final int x;
        private int y;
        private final int z;

        private BatchedZApplier(int x, int z) {
            this.x = x;
            this.z = z;
        }

        @Override
        public DensityFunction.NoisePos at(int index) {
            this.y = -64 + (index * 8);
            return this;
        }

        @Override
        public void fill(double[] densities, DensityFunction densityFunction) {
            for (int i = 0, densitiesLength = densities.length; i < densitiesLength; i++) {
                densities[i] = densityFunction.sample(this.at(i));
            }
        }

        @Override
        public int blockX() {
            return this.x;
        }

        @Override
        public int blockY() {
            return this.y;
        }

        @Override
        public int blockZ() {
            return this.z;
        }
    }

}
