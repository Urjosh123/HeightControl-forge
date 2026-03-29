package dev.ghost.heightcontrol.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.ghost.heightcontrol.manager.SelectionManager;
import dev.ghost.heightcontrol.manager.ZoneManager;
import dev.ghost.heightcontrol.model.HeightZone;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Collection;

public class HeightCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        // /hcshovel — give selection wand
        dispatcher.register(Commands.literal("hcshovel")
            .requires(s -> s.hasPermission(2))
            .executes(ctx -> giveShovel(ctx.getSource())));

        // /savezone <name>
        dispatcher.register(Commands.literal("savezone")
            .requires(s -> s.hasPermission(2))
            .then(Commands.argument("name", StringArgumentType.word())
                .executes(ctx -> saveZone(ctx.getSource(),
                    StringArgumentType.getString(ctx, "name")))));

        // /deletezone <name>
        dispatcher.register(Commands.literal("deletezone")
            .requires(s -> s.hasPermission(2))
            .then(Commands.argument("name", StringArgumentType.word())
                .executes(ctx -> deleteZone(ctx.getSource(),
                    StringArgumentType.getString(ctx, "name")))));

        // /listzones
        dispatcher.register(Commands.literal("listzones")
            .requires(s -> s.hasPermission(2))
            .executes(ctx -> listZones(ctx.getSource())));

        // /zoneinfo <name>
        dispatcher.register(Commands.literal("zoneinfo")
            .requires(s -> s.hasPermission(2))
            .then(Commands.argument("name", StringArgumentType.word())
                .executes(ctx -> zoneInfo(ctx.getSource(),
                    StringArgumentType.getString(ctx, "name")))));
    }

    // ── /hcshovel ─────────────────────────────────────────────────────────────

    private static int giveShovel(CommandSourceStack src) {
        if (!(src.getEntity() instanceof ServerPlayer player)) {
            src.sendFailure(Component.literal("Players only."));
            return 0;
        }
        ItemStack wand = new ItemStack(Items.WOODEN_SHOVEL);
        wand.setHoverName(Component.literal("§6⛏ Height Zone Selector"));
        wand.getOrCreateTag().putBoolean("hc_wand", true);
        wand.getOrCreateTag().putBoolean("Unbreakable", true);
        player.getInventory().add(wand);
        player.sendSystemMessage(Component.literal(
            "§aWand given! §7Left-click block = Pos1, Right-click block = Pos2, then §b/savezone <name>"));
        return 1;
    }

    // ── /savezone ─────────────────────────────────────────────────────────────

    private static int saveZone(CommandSourceStack src, String name) {
        if (!(src.getEntity() instanceof ServerPlayer player)) {
            src.sendFailure(Component.literal("Players only.")); return 0;
        }
        if (!name.matches("[a-zA-Z0-9_\\-]+")) {
            src.sendFailure(Component.literal("Name can only contain letters, numbers, _ and -.")); return 0;
        }
        SelectionManager.Selection sel = SelectionManager.get().get(player.getUUID());
        if (sel == null || !sel.isComplete()) {
            src.sendFailure(Component.literal("Select two positions first with /hcshovel.")); return 0;
        }

        String dim = player.level().dimension().location().toString();
        BlockPos p1 = sel.pos1, p2 = sel.pos2;

        HeightZone zone = new HeightZone(name, dim,
            p1.getX(), p1.getZ(), p2.getX(), p2.getZ());

        ZoneManager.get().add(zone);
        SelectionManager.get().clear(player.getUUID());

        src.sendSuccess(() -> Component.literal(
            "§aZone §6[" + name + "]§a saved! (" + zone.getSizeX() + "×" + zone.getSizeZ() +
            " blocks). Players can now build up to the world height limit inside it."), true);
        return 1;
    }

    // ── /deletezone ───────────────────────────────────────────────────────────

    private static int deleteZone(CommandSourceStack src, String name) {
        if (ZoneManager.get().delete(name)) {
            src.sendSuccess(() -> Component.literal("§aDeleted zone §6[" + name + "]§a."), true);
            return 1;
        }
        src.sendFailure(Component.literal("Zone not found.")); return 0;
    }

    // ── /listzones ────────────────────────────────────────────────────────────

    private static int listZones(CommandSourceStack src) {
        Collection<HeightZone> zones = ZoneManager.get().all();
        if (zones.isEmpty()) {
            src.sendSuccess(() -> Component.literal("§eNo zones saved yet."), false); return 1;
        }
        src.sendSuccess(() -> Component.literal("§6─── Height Zones (" + zones.size() + ") — full height allowed inside ───"), false);
        for (HeightZone z : zones) {
            src.sendSuccess(() -> Component.literal(
                "  §b" + z.getName() +
                " §8| §f" + z.getDimension() +
                " §8| §f" + z.getSizeX() + "×" + z.getSizeZ() + " blocks"), false);
        }
        return 1;
    }

    // ── /zoneinfo ─────────────────────────────────────────────────────────────

    private static int zoneInfo(CommandSourceStack src, String name) {
        HeightZone z = ZoneManager.get().get(name);
        if (z == null) { src.sendFailure(Component.literal("Zone not found.")); return 0; }
        src.sendSuccess(() -> Component.literal("§6─── Zone: " + z.getName() + " ───"), false);
        src.sendSuccess(() -> Component.literal("  §8Dimension: §f" + z.getDimension()), false);
        src.sendSuccess(() -> Component.literal("  §8Corner 1: §f(" + z.getMinX() + ", " + z.getMinZ() + ")"), false);
        src.sendSuccess(() -> Component.literal("  §8Corner 2: §f(" + z.getMaxX() + ", " + z.getMaxZ() + ")"), false);
        src.sendSuccess(() -> Component.literal("  §8Size: §f" + z.getSizeX() + " × " + z.getSizeZ() + " blocks"), false);
        src.sendSuccess(() -> Component.literal("  §8Normal cap: §eY " + HeightZone.NORMAL_CAP + " everywhere else"), false);
        src.sendSuccess(() -> Component.literal("  §8Inside this zone: §efull world height"), false);
        return 1;
    }
}
