package com.ds;

import com.ds.data.WaveConfig;
import com.ds.listeners.PlayerConnectionListener;
import com.ds.listeners.ServerLifecycleListener;
import net.fabricmc.api.ModInitializer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Waves implements ModInitializer {

  public static final String MOD_ID = "waves";

  public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

  public static MinecraftServer SERVER;

  public static ServerWorld OVERWORLD;

  @Override
  public void onInitialize() {
    ServerLifecycleListener.INSTANCE.register();
    PlayerConnectionListener.INSTANCE.register();

    WaveConfig.INSTANCE.init();
  }
}