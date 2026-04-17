package resolutestudios.shieldsync.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import resolutestudios.shieldsync.config.ConfigManager;

@Mixin(LivingEntity.class)
public abstract class ServerDetectionInconsistencyMixin {

    @Shadow public abstract boolean isBlocking();
    @Shadow public abstract Vec3d getRotationVec(float tickDelta);
    @Shadow public abstract Vec3d getPos();

    @Inject(method = "blockedByShield", at = @At("HEAD"), cancellable = true)
    private void shieldsync$fixDetection(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if (!ConfigManager.DETECTION_FIX_ENABLED) return;

        Entity entity = source.getSource();
        boolean piercing = false;
        if (entity instanceof PersistentProjectileEntity persistentProjectile) {
            if (persistentProjectile.getPierceLevel() > 0) {
                piercing = true;
            }
        }

        // We replicate vanilla logic but with a more lenient angle threshold for server desync.
        if (!source.isIn(DamageTypeTags.BYPASSES_SHIELD) && this.isBlocking() && !piercing) {
            Vec3d sourcePos = source.getPosition();
            if (sourcePos != null) {
                Vec3d rotation = this.getRotationVec(1.0F);
                Vec3d diff = sourcePos.relativize(this.getPos()).normalize();
                diff = new Vec3d(diff.x, 0.0, diff.z);
                
                // Vanilla uses < 0.0 (exactly 180 degrees). 
                // We expand the check to < 0.25 (approx 210 degrees) to give margin of error for turn latency.
                if (diff.dotProduct(rotation) < 0.25) {
                    cir.setReturnValue(true);
                }
            }
        }
    }
}
