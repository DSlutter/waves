package com.ds;

import com.ds.waves.WaveScheduler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Waves implements ModInitializer {
    public static final String MOD_ID = "waves";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static MinecraftServer SERVER;

    public static final World OVERWORLD = Waves.SERVER.getOverworld();


    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            SERVER = server;
            WaveScheduler.init();
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> SERVER = null);
    }
}