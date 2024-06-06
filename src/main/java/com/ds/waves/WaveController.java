package com.ds.waves;

import com.ds.Waves;
import com.ds.models.enums.WaveState;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class WaveController {
    // TODO: Create classes for waves.
//    private static Monster[][] waves;

    private static List<Wave> waves;

    private static Wave currentWave;

    private static Stack<Entity> monsters;

    private static WaveState waveState;

    private static short iteration;

    public static void init() {
        var wave = new Wave();

//        wave.setMonsters();
        waves = new ArrayList<Wave>() {{
            new Wave();
        }};

        iteration = 0;
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
        currentWave = waves.get(iteration);
        monsters = currentWave.getMonsters();

        waveState = WaveState.ONGOING;
    }

    private static void ongoing() {
        var monster = monsters.pop();

        Waves.OVERWORLD.spawnEntity(monster);
    }

    private static void defeated() {

    }

    private static void completed() {
    }
}
