package com.ishland.dfuncopto.common.debug;

import com.ishland.dfuncopto.common.IDensityFunction;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.apache.commons.io.file.PathUtils;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.atomic.AtomicLong;

public class DotExporter {

    private static final Path exportPath = FabricLoader.getInstance().getGameDir().resolve("dfuncopto-export");
    public static final AtomicLong ID = new AtomicLong(0);

    static {
        try {
            Files.createDirectories(exportPath);
            PathUtils.cleanDirectory(exportPath);
        } catch (IOException e) {
            System.err.println("Failed to create export directory");
            e.printStackTrace();
        }
    }

    public static void writeToDisk(String name, DensityFunction root) {
        try {
            final Path path = exportPath.resolve("dfuncopto-" + name + ".dot");
            Files.writeString(path, write(root), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println("Failed to write dot file");
            e.printStackTrace();
        }
    }

    public static String write(DensityFunction root) {
        final StringBuilder builder = new StringBuilder();
        builder.append("digraph ").append("TheGraph").append(" {\n");
        IntSet visitedNodes = new IntOpenHashSet();
        Long2LongMap sideWidth = new Long2LongOpenHashMap();
        write(builder, root, visitedNodes, sideWidth);
        for (Long2LongMap.Entry entry : sideWidth.long2LongEntrySet()) {
            int source = ChunkPos.getPackedX(entry.getLongKey());
            int target = ChunkPos.getPackedZ(entry.getLongKey());
            builder.append("  ").append(source).append(" -> ").append(target).append(" [label=\"").append(entry.getLongValue()).append("\"];\n");
        }

        builder.append("}");
        return builder.toString();
    }

    private static void write(StringBuilder builder, DensityFunction current, IntSet visitedNodes, Long2LongMap sideWidth) {
        // use identityHashCode for node id
        if (visitedNodes.add(System.identityHashCode(current))) {
            builder.append("  ").append(System.identityHashCode(current)).append(" [label=\"").append(getNodeName(current)).append("\"];\n");
        }
        if (current instanceof IDensityFunction<?> idf) {
            final DensityFunction[] children = idf.dfuncopto$getChildren();
            for (final DensityFunction child : children) {
                visitChild(builder, current, child, visitedNodes, sideWidth);
            }
        } else {
            // use reflection to step
            for (Field field : current.getClass().getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) continue;
                try {
                    field.setAccessible(true);
                    final Object value = field.get(current);
                    if (value instanceof final DensityFunction value1) {
                        visitChild(builder, current, value1, visitedNodes, sideWidth);
                    } else if (value instanceof DensityFunction[] children) {
                        for (final DensityFunction child : children) {
                            visitChild(builder, current, child, visitedNodes, sideWidth);
                        }
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (StackOverflowError e) {
                    System.err.println("Stack overflowed at " + getClazzName(current.getClass()) + "#" + field.getName());
                }
            }
        }
    }

    private static void visitChild(StringBuilder builder, DensityFunction current, DensityFunction child, IntSet visitedNodes, Long2LongMap sideWidth) {
//        builder.append("  ").append(System.identityHashCode(current)).append(" -> ").append(System.identityHashCode(child)).append(";\n");
        final long id = ChunkPos.toLong(System.identityHashCode(current), System.identityHashCode(child));
        sideWidth.put(id, sideWidth.getOrDefault(id, 0L) + 1);
        write(builder, child, visitedNodes, sideWidth);
    }

    private static String getNodeName(DensityFunction df) {
        StringBuilder builder = new StringBuilder();
        builder.append(System.identityHashCode(df)).append('\n');
        builder.append(getClazzName(df.getClass())).append('\n');
        for (Field field : df.getClass().getDeclaredFields()) {
            if (DensityFunction.class.isAssignableFrom(field.getType())) continue;
            try {
                field.setAccessible(true);
                builder.append(field.getName()).append(": ").append(getObj(df, field)).append('\n');
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return StringEscapeUtils.escapeJson(builder.toString().trim());
    }

    private static String getObj(DensityFunction df, Field field) throws IllegalAccessException {
        final Object obj = field.get(df);
        if (field.getType().isPrimitive() || field.getType().isEnum()) {
            return String.valueOf(obj);
        }
        if (obj instanceof DensityFunction.Noise noise) {
            return String.format("Noise{%s}", noise.noiseData().getKey().map(RegistryKey::toString).orElseGet(() -> String.valueOf(noise.noiseData().value())));
        }
        return getClazzName(field.getType());
    }

    private static String getClazzName(Class<?> clazz) {
        final String[] split = clazz.getName().split("\\.");
        return split.length == 0 ? clazz.getName() : split[split.length - 1];
    }

}
