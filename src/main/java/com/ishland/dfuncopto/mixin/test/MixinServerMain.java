package com.ishland.dfuncopto.mixin.test;

import com.ishland.dfuncopto.common.test.TestStandalone;
import net.minecraft.server.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Main.class)
public class MixinServerMain {

    @Inject(method = "main", at = @At("HEAD"))
    private static void onInit(String[] args, CallbackInfo ci) {
        TestStandalone.main(args);
        System.exit(0);
    }

}
