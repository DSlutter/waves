package com.ds.data;

import com.ds.Waves;
import com.ds.models.Wave;
import com.ds.models.WaveSpawn;
import com.ds.serializers.ISerializer;
import com.ds.serializers.JsonSerializer;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.EntityType;

public class WaveConfig {

  public static final WaveConfig INSTANCE = new WaveConfig();

  private final String FILE_NAME = Waves.MOD_ID + ".json";

  private final Path path = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);

  private final File file = path.toFile();

  private final ISerializer serializer;

  public WaveConfig() {
    serializer = new JsonSerializer();
  }

  public void init() {
    if (file.exists()) {
      return;
    }

    writeDefaultWaveSpawnConfig();
  }

  public WaveSpawn[] getWaveSpawnConfig() {
    try {
      var json = Files.readString(path);
      return serializer.deserialize(WaveSpawn[].class, json);
    } catch (IOException exception) {
      String exceptionMessage = String.format(
          "Exception thrown in WaveConfig.writeDefaultWaveData: %s",
          exception.getMessage());
      Waves.LOGGER.error(exceptionMessage);

      return new WaveSpawn[]{new WaveSpawn(new ArrayList<>())};
    }
  }

  private void writeDefaultWaveSpawnConfig() {
    List<WaveSpawn> defaultData = createDefaultWaveSpawnConfig();

    try (FileWriter writer = new FileWriter(file)) {
      var json = serializer.serialize(defaultData);
      writer.write(json);
    } catch (IOException exception) {
      String exceptionMessage = String.format(
          "Exception thrown in WaveConfig.writeDefaultWaveData: %s",
          exception.getMessage());
      Waves.LOGGER.error(exceptionMessage);
    }
  }

  private List<WaveSpawn> createDefaultWaveSpawnConfig() {
    Stack<EntityType<?>> monsters = new Stack<>();
    monsters.add(EntityType.ZOMBIE);
    monsters.add(EntityType.SKELETON);

    Wave wave = new Wave(monsters);
    List<Wave> waves = List.of(wave);

    return List.of(new WaveSpawn(waves), new WaveSpawn(waves), new WaveSpawn(waves),
        new WaveSpawn(waves), new WaveSpawn(waves), new WaveSpawn(waves));
  }
}
