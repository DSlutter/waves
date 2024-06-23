package com.ds.waves;

import com.ds.Waves;
import com.ds.constants.WaveConstants;
import com.ds.exceptions.PlayerNotFoundException;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.server.network.ServerPlayerEntity;

public class WaveScheduler implements Runnable {

  private final AtomicBoolean running = new AtomicBoolean(false);

  private final ServerPlayerEntity serverPlayer;

  private final Random random = new Random();

  private WaveController waveController;

  private int spawnTime = -1;

  private boolean checkedForSpawn;

  private boolean shouldSpawn;

  public WaveScheduler(ServerPlayerEntity serverPlayer) {
    this.serverPlayer = serverPlayer;
  }

  public ServerPlayerEntity getServerPlayer() {
    return serverPlayer;
  }

  public void shutdown() {
    Waves.LOGGER.debug("WaveScheduler: shutting down");
    running.set(false);
    if (waveController != null && waveController.isRunning()) {
      waveController.shutdown();
      waveController = null;
    }
  }

  @Override
  public void run() {
    running.set(true);
    try {
      start();
    } catch (InterruptedException | PlayerNotFoundException ex) {
      Waves.LOGGER.error(ex.getMessage());
      running.set(false);
    }
  }

  private void start() throws InterruptedException, PlayerNotFoundException {
    if (serverPlayer == null) {
      throw new PlayerNotFoundException("WaveScheduler: player is null.");
    }

    while (running.get()) {
      // There are 2 seconds between each update.
      TimeUnit.SECONDS.sleep(2);
      update();
    }
  }

  private void update() {
    if (waveController != null && waveController.isRunning()) {
      return;
    } else if (waveController != null) {
      waveController = null;
    }

    var timeOfDay = Waves.OVERWORLD.getTimeOfDay() % 24000;
    var canSpawn = canSpawn(timeOfDay);

    if (checkedForSpawn && !canSpawn) {
      checkedForSpawn = false;

      return;
    }

    if (!checkedForSpawn && canSpawn) {
      checkedForSpawn = true;

      shouldSpawn = shouldSpawn();

      if (shouldSpawn) {
        spawnTime = getRandomSpawnTime();
        Waves.LOGGER.debug("SHOULD ON: " + spawnTime);
      }

      return;
    }

    if (shouldSpawn && timeOfDay >= spawnTime) {
      shouldSpawn = false;
      Waves.LOGGER.debug("WAVE");
      waveController = new WaveController(serverPlayer);
      Thread.ofVirtual().start(waveController);
    }
  }

  private boolean canSpawn(long timeOfDay) {
    return timeOfDay >= WaveConstants.SPAWN_FROM_TIME && timeOfDay <= WaveConstants.SPAWN_TILL_TIME;
  }

  private boolean shouldSpawn() {
    random.nextFloat();
    return true;
  }

  private int getRandomSpawnTime() {
    return random.nextInt(WaveConstants.SPAWN_FROM_TIME, WaveConstants.SPAWN_TILL_TIME);
  }
}
