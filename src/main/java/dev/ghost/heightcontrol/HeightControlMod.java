package dev.ghost.heightcontrol;

import dev.ghost.heightcontrol.command.HeightCommands;
import dev.ghost.heightcontrol.listener.WandListener;
import dev.ghost.heightcontrol.manager.ZoneManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("heightcontrol")
public class HeightControlMod {

    private static final Logger LOGGER = LogManager.getLogger("HeightControl");

    public HeightControlMod() {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new WandListener());
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        ZoneManager.get().init(FMLPaths.CONFIGDIR.get().resolve("heightcontrol"));
        LOGGER.info("HeightControl ready. {} zone(s) loaded. Normal cap: Y {}.",
            ZoneManager.get().all().size(), dev.ghost.heightcontrol.model.HeightZone.NORMAL_CAP);
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        ZoneManager.get().save();
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        HeightCommands.register(event.getDispatcher());
    }
}
