package com.ishland.dfuncopto.common;

import cpufeatures.CpuFeatures;
import cpufeatures.aarch64.Aarch64Feature;
import cpufeatures.arm.ArmFeature;
import cpufeatures.riscv.RiscvFeature;
import cpufeatures.riscv.RiscvFeatures;
import cpufeatures.x86.X86Feature;

public class SharedConstants {

    public static final String INVALID_ORIGINAL_DFUNC = "Original density function is not a child of this density function";

    public static boolean hasFMA;

    static {
        boolean hasFMA = false;
        try {
            final String property = System.getProperty("joml.useMathFma");
            if (property != null) {
                if (isOptionEnabled(property)) {
                    System.out.println("FMA: enabled by joml.useMathFma");
                    hasFMA = true;
                } else {
                    System.out.println("FMA: disabled by joml.useMathFma");
                    hasFMA = false;
                }
            } else {
                CpuFeatures.load();
                hasFMA = switch (CpuFeatures.getArchitecture()) {
                    case UNSUPPORTED -> false;
                    case AARCH64 -> CpuFeatures.getAarch64Info().has(Aarch64Feature.ASIMD);
                    case ARM -> CpuFeatures.getArmInfo().has(ArmFeature.NEON);
                    case RISCV -> true;
                    case X86 -> CpuFeatures.getX86Info().has(X86Feature.FMA3);
                };
            }
        } catch (Throwable t) {
            System.err.println("Failed to determine FMA support, assuming no FMA support");
            t.printStackTrace();
            hasFMA = false;
        }
        SharedConstants.hasFMA = hasFMA;
        System.out.println("FMA: " + hasFMA);
    }

    private static boolean isOptionEnabled(String v) {
        if (v.trim().length() == 0)
            return true;
        return Boolean.valueOf(v).booleanValue();
    }

}
