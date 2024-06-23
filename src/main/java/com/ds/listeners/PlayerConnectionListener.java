package com.ds.listeners;

import com.ds.Waves;
import com.ds.waves.WaveScheduler;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayNetworkHandler;

public class PlayerConnectionListener {

  public static final PlayerConnectionListener INSTANCE = new PlayerConnectionListener();

  private static List<WaveScheduler> waveSchedulers;

  public void register() {
    waveSchedulers = new ArrayList<>();

    ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> handleJoin(handler));

    ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> handleDisconnect(handler));
  }

  private PlayerConnectionListener() {
  }

  private static void handleJoin(ServerPlayNetworkHandler handler) {
    var serverPlayer = handler.getPlayer();

    Waves.LOGGER.debug(
        "PlayerConnectionListener: TRYING TO CONNECT SCHEDULER FOR PLAYER: "
            + serverPlayer.getName());

    var waveScheduler = new WaveScheduler(serverPlayer);

    waveSchedulers.add(waveScheduler);

    Thread.ofVirtual().start(waveScheduler);

    Waves.LOGGER.debug(
        "PlayerConnectionListener: CONNECTED SCHEDULER FOR PLAYER: " + serverPlayer.getName());
  }

  private static void handleDisconnect(ServerPlayNetworkHandler handler) {
    var serverPlayer = handler.getPlayer();

    Waves.LOGGER.debug(
        "PlayerConnectionListener: TRYING TO DISCONNECT SCHEDULER FOR PLAYER: "
            + serverPlayer.getName());

    var waveScheduler = waveSchedulers.stream()
        .filter(x -> x.getServerPlayer().getUuid() == serverPlayer.getUuid())
        .findFirst().orElse(null);

    if (waveScheduler == null) {
      return;
    }

    waveScheduler.shutdown();

    waveSchedulers.remove(waveScheduler);

    Waves.LOGGER.debug(
        "PlayerConnectionListener: DISCONNECTED SCHEDULER FOR PLAYER: " + serverPlayer.getName());
  }
}
