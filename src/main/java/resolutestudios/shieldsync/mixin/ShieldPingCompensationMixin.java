package resolutestudios.shieldsync.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.item.consume.UseAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import resolutestudios.shieldsync.config.ConfigManager;

@Mixin(LivingEntity.class)
public class ShieldPingCompensationMixin {

    @Inject(method = "isBlocking", at = @At("HEAD"), cancellable = true)
    private void shieldsync$compensatePing(CallbackInfoReturnable<Boolean> cir) {
        if (!ConfigManager.ENABLED) return;

        LivingEntity thisEntity = (LivingEntity) (Object) this;

        if (thisEntity instanceof ServerPlayerEntity player) {
            if (player.isUsingItem() && !player.getActiveItem().isEmpty()) {
                Item item = player.getActiveItem().getItem();
                
                // Assuming standard use action block check
                if (item.getUseAction(player.getActiveItem()) == UseAction.BLOCK) {
                    int ping = player.networkHandler.getLatency();
                    int compensationMs = Math.min(ConfigManager.MAX_PING_COMPENSATION_MS, ping);
                    int compensationTicks = compensationMs / 50; 
                    int requiredTicks = Math.max(0, 5 - compensationTicks);

                    if (item.getMaxUseTime(player.getActiveItem(), player) - player.getItemUseTimeLeft() >= requiredTicks) {
                        cir.setReturnValue(true);
                    }
                }
            }
        }
    }
}
