package com.ds.serializers.custom;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import net.minecraft.util.Identifier;

import java.io.IOException;

public class IdentifierDeserializer extends JsonDeserializer<Identifier> {
    @Override
    public Identifier deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String identifierString = p.getValueAsString();
        return new Identifier(identifierString);
    }
}
