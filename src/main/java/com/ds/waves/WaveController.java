package com.ds.waves;

//import com.ds.Waves;

import com.ds.Waves;
import com.ds.models.Wave;
import com.ds.models.WaveSpawn;
import com.ds.models.enums.WaveState;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.Random;
import java.util.Stack;

public class WaveController {
    private static final Random random = new Random();

    private static WaveSpawn waveSpawn;

    private static Wave currentWave;

    private static Stack<EntityType<?>> monsters;

    private static WaveState waveState;

    private static short iteration;

    private static PlayerEntity player;

    public static void init() {
        player = Waves.OVERWORLD.getPlayers().stream().findFirst().orElse(null);
        if (player == null) {
            Waves.LOGGER.error("No player found to spawn monsters near.");
            return;
        }
        waveSpawn = Waves.WAVE_SPAWNS[0];
        iteration = 0;
        waveState = WaveState.STARTED;
        ServerTickEvents.END_WORLD_TICK.register(server -> update());
    }

    private static void update() {
        switch (waveState) {
            case STARTED -> started();
            case ONGOING -> ongoing();
            case DEFEATED -> defeated();
            case COMPLETED -> completed();
        }
    }

    private static void started() {
        Waves.LOGGER.error("WaveController: started");
        currentWave = waveSpawn.waves().get(iteration);
        monsters = currentWave.monsters();

        waveState = WaveState.ONGOING;
    }

    private static void ongoing() {
        Waves.LOGGER.error("WaveController: ongoing");
        if (monsters.isEmpty()) {
            // TODO: Find way to track spawned monsters.
            waveState = WaveState.DEFEATED;
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
    }

    private static void defeated() {
        Waves.LOGGER.error("WaveController: defeated");
        iteration++;
        if (waveSpawn.waves().size() <= iteration) {
            waveState = WaveState.COMPLETED;
            return;
        }

        waveState = WaveState.STARTED;
    }

    private static void completed() {
        Waves.LOGGER.error("WaveController: completed");
    }


    private static BlockPos getRandomPositionAroundPlayer(PlayerEntity player) {
        int radius = 10; // Radius around the player to spawn monsters
        double angle = random.nextDouble() * 2 * Math.PI;
        int offsetX = (int) (radius * Math.cos(angle));
        int offsetZ = (int) (radius * Math.sin(angle));
        BlockPos playerPos = player.getBlockPos();
        return playerPos.add(offsetX, 0, offsetZ);
    }
}
