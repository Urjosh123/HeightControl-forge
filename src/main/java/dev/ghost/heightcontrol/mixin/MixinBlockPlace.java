package dev.ghost.heightcontrol.mixin;

import dev.ghost.heightcontrol.manager.ZoneManager;
import dev.ghost.heightcontrol.model.HeightZone;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.world.level.Level;

@Mixin(Level.class)
public abstract class MixinBlockPlace {

    /**
     * Intercept setBlock. If the Y position is above NORMAL_CAP and the XZ is
     * NOT inside a registered zone, cancel the placement and notify the player.
     *
     * We inject into setBlock(BlockPos, BlockState, int, int) which is the
     * lowest-level method all block placements funnel through on the server.
     */
    @Inject(
        method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private void heightcontrol_setBlock(BlockPos pos, BlockState state,
                                         int flags, int recursionLeft,
                                         CallbackInfoReturnable<Boolean> cir) {
        // Only run on the server
        Level self = (Level)(Object)this;
        if (self.isClientSide()) return;

        int y = pos.getY();
        if (y < HeightZone.NORMAL_CAP) return; // below normal cap, always fine

        String dim = self.dimension().location().toString();

        // If inside a zone, full height is allowed — do nothing
        if (ZoneManager.get().isInZone(pos.getX(), pos.getZ(), dim)) return;

        // Outside a zone and above the cap — cancel
        cir.setReturnValue(false);

        // Try to notify the player who caused this (best-effort)
        if (self instanceof ServerLevel serverLevel) {
            // Find nearest player to the block position to send feedback
            ServerPlayer nearest = serverLevel.getNearestPlayer(
                pos.getX(), pos.getY(), pos.getZ(), 10, false);
            if (nearest != null) {
                nearest.sendSystemMessage(Component.literal(
                    "§cYou can't build above Y " + HeightZone.NORMAL_CAP +
                    " here. Create a zone with §6/hcshovel§c first."));
            }
        }
    }
}
