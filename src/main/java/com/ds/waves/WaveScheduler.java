package com.ds.waves;

import com.ds.Waves;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.world.World;

import java.util.Random;

public class WaveScheduler {

    private static final World world = Waves.SERVER.getOverworld();

    private static final Random random = new Random();

    private static final int fromTime = 13000;

    private static final int toTime = 23000;

    public static void start() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            Waves.LOGGER.warn("TEST");
            if (canSpawn() && shouldSpawn()) {
                Waves.LOGGER.warn("WAVE");
            }
        });
    }

    private static boolean canSpawn() {
        var timeOfDay = world.getTime() % 24000;

        // Minecraft considers night to be from 12541 to 23458 ticks into the day
        return timeOfDay > fromTime && timeOfDay < toTime;
    }

    private static boolean shouldSpawn() {
        return random.nextFloat() < 0.33f;
    }

    private long generateRandomNightTime() {
        // Generate a random time during the night (e.g., between 13000 and 23000 ticks)
        return 13000L + random.nextInt(10001);
    }
}
