package resolutestudios.shieldsync.mixin;

import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import resolutestudios.shieldsync.config.ConfigManager;

@Mixin(PlayerEntity.class)
public class AxeShieldFixMixin {

    @Unique
    private boolean shieldsync$blockedThisTick = false;

    @Inject(method = "damageShield", at = @At("HEAD"))
    private void shieldsync$onDamageShield(float amount, CallbackInfo ci) {
        if (ConfigManager.AXE_FIX_ENABLED) {
            this.shieldsync$blockedThisTick = true;
        }
    }

    @Inject(method = "disableShield", at = @At("HEAD"), cancellable = true)
    private void shieldsync$onDisableShield(boolean sprinting, CallbackInfo ci) {
        if (ConfigManager.AXE_FIX_ENABLED && !this.shieldsync$blockedThisTick) {
            // The shield wasn't actually damaged/used to block this tick, so it shouldn't be disabled!
            // This prevents axes disabling the shield when it didn't intercept the hit.
            ci.cancel();
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void shieldsync$onTick(CallbackInfo ci) {
        this.shieldsync$blockedThisTick = false;
    }
}
