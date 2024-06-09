package com.ds.waves;

import com.ds.Waves;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import java.util.Random;

public class WaveScheduler {
    private static final Random random = new Random();

    private static final int fromTime = 13000;

    private static final int toTime = 23000;

    private static long timeOfDay = 1;

    private static int spawnTime = 0;

    private static boolean checked;

    private static boolean shouldSpawn;

    public static void init() {
        ServerTickEvents.END_WORLD_TICK.register(server -> tick());
    }

    private static void tick() {
        timeOfDay = Waves.OVERWORLD.getTimeOfDay() % 24000;
        var canSpawn = canSpawn();

        if (checked && !canSpawn) {
            checked = false;

            return;
        }

        if (!checked && canSpawn) {
            checked = true;

            shouldSpawn = shouldSpawn();

            if (shouldSpawn) {
                spawnTime = getRandomSpawnTime();
                Waves.LOGGER.warn("SHOULD ON: " + spawnTime);
            }

            return;
        }

        if (shouldSpawn && timeOfDay >= spawnTime) {
            shouldSpawn = false;
            Waves.LOGGER.warn("WAVE");
            WaveController.init();
        }
    }

    private static boolean canSpawn() {
        // Minecraft considers night to be from 12541 to 23458 ticks into the day
        return timeOfDay >= fromTime && timeOfDay <= toTime;
    }

    private static boolean shouldSpawn() {
        return random.nextFloat() < 0.25f || true;
    }

    private static int getRandomSpawnTime() {
        // Generate a random time during the night (e.g., between 13000 and 23000 ticks)
        return 13000 + random.nextInt(10000);
    }
}
