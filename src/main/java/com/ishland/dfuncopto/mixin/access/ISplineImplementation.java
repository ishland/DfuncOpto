package com.ishland.dfuncopto.mixin.access;

import net.minecraft.util.function.ToFloatFunction;
import net.minecraft.util.math.Spline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Spline.Implementation.class)
public interface ISplineImplementation<C, I extends ToFloatFunction<C>> {

    @Mutable
    @Accessor
    void setLocationFunction(I locationFunction);

}
