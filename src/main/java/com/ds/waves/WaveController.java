package com.ds.waves;

import com.ds.Waves;
import com.ds.data.WaveConfig;
import com.ds.models.WaveSpawn;
import com.ds.models.enums.WaveState;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;

public class WaveController implements Runnable {

  private final AtomicBoolean running = new AtomicBoolean(false);

  private final Random random = new Random();

  private WaveSpawn waveSpawn;

  private Stack<EntityType<?>> monsters;

  private List<Entity> createdMonsters;

  private WaveState waveState;

  private short iteration;

  private ServerPlayerEntity player;

  @Override
  public void run() {
    running.set(true);
    try {
      start();
    } catch (InterruptedException iex) {
      Waves.LOGGER.error(iex.getMessage());
      running.set(false);
    }
  }

  public void start() throws InterruptedException {
    player = Waves.OVERWORLD.getPlayers().stream().findFirst().orElse(null);
    if (player == null) {
      Waves.LOGGER.error("No player found to spawn monsters near.");
      return;
    }
    waveSpawn = WaveConfig.INSTANCE.getWaveSpawnConfig()[0];
    iteration = 0;
    createdMonsters = new ArrayList<>();
    waveState = WaveState.STARTED;

    while (running.get()) {
      // There are 2 seconds between each update.
      Waves.LOGGER.warn("SLEEP 2 SECONDS");
      TimeUnit.SECONDS.sleep(2);
      Waves.LOGGER.warn("WAKE UP AND PERFORM UPDATE");
      update();
    }
  }

  private void update() throws InterruptedException {
    switch (waveState) {
      case STARTED -> started();
      case ONGOING -> ongoing();
      case DEFEATED -> defeated();
      case COMPLETED -> completed();
      case SHUTDOWN -> shutdown();
    }
  }

  private void started() {
    Waves.LOGGER.error("WaveController: started");
    var currentWave = waveSpawn.waves().get(iteration);
    monsters = currentWave.monsters();

    waveState = WaveState.ONGOING;
  }

  private void ongoing() {
    Waves.LOGGER.error("WaveController: ongoing");
    if (monsters.isEmpty()) {
      Waves.LOGGER.error("WaveController: no more monsters to spawn");

      if (waveCompleted()) {
        Waves.LOGGER.error("WaveController: ALL ENTITIES KILLED");
        waveState = WaveState.DEFEATED;
      }

      return;
    }

    var monster = monsters.pop();
    var entity = monster.create(Waves.OVERWORLD);
    if (entity == null) {
      Waves.LOGGER.error("Entity is null.");
      return;
    }

    var spawnPos = getRandomPositionAroundPlayer(player);
    entity.refreshPositionAndAngles(spawnPos, 0.0F, 0.0F);
    Waves.OVERWORLD.spawnEntity(entity);

    createdMonsters.add(entity);
  }

  private void defeated() throws InterruptedException {
    Waves.LOGGER.error("WaveController: defeated");
    iteration++;
    if (waveSpawn.waves().size() <= iteration) {
      waveState = WaveState.COMPLETED;
      return;
    }

    displayTitle("Waiting on the next wave", player);
    Waves.LOGGER.warn("SLEEP 6 SECONDS BEFORE NEXT WAVE");
    TimeUnit.SECONDS.sleep(6);
    Waves.LOGGER.warn("WAKE UP AND PERFORM UPDATE");
    waveState = WaveState.STARTED;
  }

  private void completed() {
    Waves.LOGGER.error("WaveController: completed");
    displayTitle("Waves completed", player);

    waveState = WaveState.SHUTDOWN;
  }

  private void shutdown() {
    running.set(false);
  }


  private boolean waveCompleted() {
    return createdMonsters.stream().noneMatch(Entity::isAlive);
  }


  private BlockPos getRandomPositionAroundPlayer(PlayerEntity player) {
    var radius = 10; // Radius around the player to spawn monsters
    var angle = random.nextDouble() * 2 * Math.PI;
    var offsetX = (int) (radius * Math.cos(angle));
    var offsetZ = (int) (radius * Math.sin(angle));

    var playerPos = player.getBlockPos();
    var x = playerPos.getX() + offsetX;
    var z = playerPos.getZ() + offsetZ;
    var y = Waves.OVERWORLD.getTopY(Heightmap.Type.WORLD_SURFACE, x,
        z); // Get the Top Y coordinate to prevent spawning inside walls.

    return new BlockPos(x, y, z);
  }

  private void displayTitle(String title, ServerPlayerEntity serverPlayer) {
    serverPlayer.networkHandler.sendPacket(
        new TitleS2CPacket(Text.of(title))
    );
  }
}
