package com.ds.waves;

//import com.ds.Waves;

import com.ds.Waves;
import com.ds.models.Wave;
import com.ds.models.enums.WaveState;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.Stack;

public class WaveController {
    private static Wave currentWave;

    private static Stack<Identifier> monsters;

    private static WaveState waveState;

    private static short iteration;

    public static void init() {
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

        currentWave = Waves.WAVE_SPAWNS[0].waves().get(iteration);
        monsters = currentWave.monsters();

        waveState = WaveState.ONGOING;
    }

    private static void ongoing() {
        Waves.LOGGER.error("WaveController: ongoing");

        var monster = monsters.pop();

        Waves.OVERWORLD.spawnEntity(Registries.ENTITY_TYPE.get(monster).create(Waves.OVERWORLD));
    }

    private static void defeated() {
        iteration++;
    }

    private static void completed() {
    }
}
