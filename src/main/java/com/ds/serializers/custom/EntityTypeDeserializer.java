package com.ds.serializers.custom;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.io.IOException;

public class EntityTypeDeserializer extends JsonDeserializer<EntityType<?>> {
    @Override
    public EntityType<?> deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String idStr = parser.getValueAsString();
        Identifier id = new Identifier(idStr);
        return Registries.ENTITY_TYPE.get(id);
    }
}
