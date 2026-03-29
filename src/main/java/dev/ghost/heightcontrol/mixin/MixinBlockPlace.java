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
     * Intercept setBlock and enforce height caps.
     *
     * Logic:
     *  - INSIDE a zone with a custom maxY  → deny if y >= zone.maxY
     *  - INSIDE a zone with no maxY set    → allow (full world height)
     *  - OUTSIDE all zones                 → deny if y >= HeightZone.NORMAL_CAP
     */
    @Inject(
        method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private void heightcontrol_setBlock(BlockPos pos, BlockState state,
                                         int flags, int recursionLeft,
                                         CallbackInfoReturnable<Boolean> cir) {
        Level self = (Level)(Object)this;
        if (self.isClientSide()) return;

        int y = pos.getY();
        String dim = self.dimension().location().toString();

        HeightZone zone = ZoneManager.get().getZoneAt(pos.getX(), pos.getZ(), dim);

        if (zone != null) {
            // Inside a zone — only enforce if this zone has a custom cap
            if (!zone.hasHeightLimit()) return; // no cap, full height allowed
            if (y < zone.getMaxY()) return;     // below this zone's cap, fine

            // Above the zone's cap — cancel and notify
            cir.setReturnValue(false);
            notifyPlayer(self, pos,
                "§cYou can't build above Y " + zone.getMaxY() +
                " in zone §6[" + zone.getName() + "]§c.");
        } else {
            // Outside all zones — enforce global NORMAL_CAP
            if (y < HeightZone.NORMAL_CAP) return;

            cir.setReturnValue(false);
            notifyPlayer(self, pos,
                "§cYou can't build above Y " + HeightZone.NORMAL_CAP +
                " here. Use §6/hcshovel§c to define a zone first.");
        }
    }

    private static void notifyPlayer(Level level, BlockPos pos, String message) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        ServerPlayer nearest = serverLevel.getNearestPlayer(
            pos.getX(), pos.getY(), pos.getZ(), 10, false);
        if (nearest != null) {
            nearest.sendSystemMessage(Component.literal(message));
        }
    }
}
