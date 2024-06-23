package com.ds.listeners;

import com.ds.Waves;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

public class ServerLifecycleListener {

  public static final ServerLifecycleListener INSTANCE = new ServerLifecycleListener();

  public void register() {
    ServerLifecycleEvents.SERVER_STARTED.register(this::handleStarted);
    ServerLifecycleEvents.SERVER_STOPPED.register(server -> handleStopped());
  }

  private ServerLifecycleListener() {
  }

  private void handleStarted(MinecraftServer server) {
    Waves.SERVER = server;
    Waves.OVERWORLD = Waves.SERVER.getOverworld();
  }

  private void handleStopped() {
    Waves.SERVER = null;
  }
}
