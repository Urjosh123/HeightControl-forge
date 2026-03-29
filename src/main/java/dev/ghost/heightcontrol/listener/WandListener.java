package dev.ghost.heightcontrol.listener;

import dev.ghost.heightcontrol.manager.SelectionManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class WandListener {

    private static boolean isWand(ItemStack stack) {
        if (stack.isEmpty()) return false;
        var tag = stack.getTag();
        return tag != null && tag.getBoolean("hc_wand");
    }

    @SubscribeEvent
    public void onLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!isWand(player.getMainHandItem())) return;

        event.setCanceled(true);
        BlockPos pos = event.getPos();
        String dim = player.level().dimension().location().toString();
        SelectionManager.get().setPos1(player.getUUID(), pos, dim);
        player.sendSystemMessage(Component.literal(
            "§6✔ Pos1: §e(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")"));
        sendStatus(player);
    }

    @SubscribeEvent
    public void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!isWand(player.getMainHandItem())) return;

        event.setCanceled(true);
        BlockPos pos = event.getPos();
        String dim = player.level().dimension().location().toString();
        SelectionManager.get().setPos2(player.getUUID(), pos, dim);
        player.sendSystemMessage(Component.literal(
            "§6✔ Pos2: §e(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")"));
        sendStatus(player);
    }

    private void sendStatus(ServerPlayer player) {
        SelectionManager.Selection sel = SelectionManager.get().get(player.getUUID());
        if (sel != null && sel.isComplete()) {
            int sx = Math.abs(sel.pos2.getX() - sel.pos1.getX()) + 1;
            int sz = Math.abs(sel.pos2.getZ() - sel.pos1.getZ()) + 1;
            player.sendSystemMessage(Component.literal(
                "§7  " + sx + "×" + sz + " blocks selected. Run §b/savezone <n>§7 to save."));
        }
    }
}
