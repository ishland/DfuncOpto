package com.ishland.dfuncopto.common.test;

import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.ServerDynamicRegistryType;
import net.minecraft.resource.DataConfiguration;
import net.minecraft.server.DataPackContents;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionOptionsRegistryHolder;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.level.WorldGenSettings;

import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

public record GeneratorOptionsHolder(
        GeneratorOptions generatorOptions,
        Registry<DimensionOptions> dimensionOptionsRegistry,
        DimensionOptionsRegistryHolder selectedDimensions,
        CombinedDynamicRegistries<ServerDynamicRegistryType> combinedDynamicRegistries,
        DataPackContents dataPackContents,
        DataConfiguration dataConfiguration
) {
    public GeneratorOptionsHolder(
            WorldGenSettings worldGenSettings,
            CombinedDynamicRegistries<ServerDynamicRegistryType> combinedDynamicRegistries,
            DataPackContents dataPackContents,
            DataConfiguration dataConfiguration
    ) {
        this(worldGenSettings.generatorOptions(), worldGenSettings.dimensionOptionsRegistryHolder(), combinedDynamicRegistries, dataPackContents, dataConfiguration);
    }

    public GeneratorOptionsHolder(
            GeneratorOptions generatorOptions,
            DimensionOptionsRegistryHolder selectedDimensions,
            CombinedDynamicRegistries<ServerDynamicRegistryType> combinedDynamicRegistries,
            DataPackContents dataPackContents,
            DataConfiguration dataConfiguration
    ) {
        this(
                generatorOptions,
                combinedDynamicRegistries.get(ServerDynamicRegistryType.DIMENSIONS).get(RegistryKeys.DIMENSION),
                selectedDimensions,
                combinedDynamicRegistries.with(ServerDynamicRegistryType.DIMENSIONS),
                dataPackContents,
                dataConfiguration
        );
    }

    public net.minecraft.client.world.GeneratorOptionsHolder with(GeneratorOptions generatorOptions, DimensionOptionsRegistryHolder selectedDimensions) {
        return new net.minecraft.client.world.GeneratorOptionsHolder(
                generatorOptions, this.dimensionOptionsRegistry, selectedDimensions, this.combinedDynamicRegistries, this.dataPackContents, this.dataConfiguration
        );
    }

    public net.minecraft.client.world.GeneratorOptionsHolder apply(Modifier modifier) {
        return new net.minecraft.client.world.GeneratorOptionsHolder(
                (GeneratorOptions)modifier.apply(this.generatorOptions),
                this.dimensionOptionsRegistry,
                this.selectedDimensions,
                this.combinedDynamicRegistries,
                this.dataPackContents,
                this.dataConfiguration
        );
    }

    public net.minecraft.client.world.GeneratorOptionsHolder apply(RegistryAwareModifier modifier) {
        return new net.minecraft.client.world.GeneratorOptionsHolder(
                this.generatorOptions,
                this.dimensionOptionsRegistry,
                (DimensionOptionsRegistryHolder)modifier.apply(this.getCombinedRegistryManager(), this.selectedDimensions),
                this.combinedDynamicRegistries,
                this.dataPackContents,
                this.dataConfiguration
        );
    }

    public DynamicRegistryManager.Immutable getCombinedRegistryManager() {
        return this.combinedDynamicRegistries.getCombinedRegistryManager();
    }

    public interface Modifier extends UnaryOperator<GeneratorOptions> {
    }

    @FunctionalInterface
    public interface RegistryAwareModifier extends BiFunction<DynamicRegistryManager.Immutable, DimensionOptionsRegistryHolder, DimensionOptionsRegistryHolder> {
    }
}