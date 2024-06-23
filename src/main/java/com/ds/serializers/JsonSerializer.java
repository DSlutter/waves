package com.ds.serializers;

import com.ds.Waves;
import com.ds.serializers.custom.EntityTypeDeserializer;
import com.ds.serializers.custom.EntityTypeSerializer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import net.minecraft.entity.EntityType;

public class JsonSerializer implements ISerializer {

  private final JsonMapper mapper;

  public JsonSerializer() {
    this.mapper = new JsonMapper();
    configureMapper();
  }

  @Override
  public <T> String serialize(T obj) {
    try {
      return mapper.writeValueAsString(obj);
    } catch (JsonProcessingException exception) {
      String exceptionMessage = String.format("Exception thrown in JsonSerializer.serialize: %s",
          exception.getMessage());
      Waves.LOGGER.error(exceptionMessage);
      return null;
    }
  }

  @Override
  public <T> T deserialize(Class<T> t, String json) {
    try {
      var type = mapper.constructType(t);
      return mapper.readerFor(type).readValue(json);
    } catch (JsonProcessingException exception) {
      String exceptionMessage = String.format("Exception thrown in JsonSerializer.deserialize: %s",
          exception.getMessage());
      Waves.LOGGER.error(exceptionMessage);
      return null;
    }
  }

  private void configureMapper() {
    SimpleModule module = new SimpleModule()
        .addSerializer((Class<EntityType<?>>) (Class<?>) EntityType.class,
            new EntityTypeSerializer())
        .addDeserializer((Class<EntityType<?>>) (Class<?>) EntityType.class,
            new EntityTypeDeserializer());

    mapper.registerModule(module);
    mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
    mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, false);
  }
}
