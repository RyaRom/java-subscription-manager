package backend.academy.proto;

import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import java.nio.ByteBuffer;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RedisProtoCodec<K, V> implements RedisCodec<K, V> {
    private final ByteArrayCodec byteArrayCodec;

    @Override
    public K decodeKey(ByteBuffer byteBuffer) {
        return null;
    }

    @Override
    public V decodeValue(ByteBuffer byteBuffer) {
        return null;
    }

    @Override
    public ByteBuffer encodeKey(K k) {
        return null;
    }

    @Override
    public ByteBuffer encodeValue(V v) {
        return null;
    }
}
