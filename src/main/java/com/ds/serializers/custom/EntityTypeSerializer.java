package com.ds.serializers.custom;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.io.IOException;

public class EntityTypeSerializer extends JsonSerializer<EntityType<?>> {
    @Override
    public void serialize(EntityType<?> entityType, JsonGenerator generator, SerializerProvider provider) throws IOException {
        Identifier id = Registries.ENTITY_TYPE.getId(entityType);
        generator.writeString(id.toString());
    }
}
