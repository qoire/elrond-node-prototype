package network.elrond.data;

public interface SerializationService {

    <T> String encodeJSON(T object);

    <T> T decodeJSON(String strJSONData, Class<T> clazz);
}