package com.ds.serializers;

public interface ISerializer {

  /**
   * Serializes the object to text
   *
   * @param <T> The type of the object to serialize
   * @param obj The object to serialize
   * @return The serialized object
   */
  <T> String serialize(T obj);

  /**
   * Deserializes the text to the object of the given type
   *
   * @param t    The type to deserialize the object to
   * @param json The json to deserialize
   * @return The deserialized object
   */
  <T> T deserialize(Class<T> t, String json);
}
