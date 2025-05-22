package backend.academy.service;

import com.google.protobuf.GeneratedMessage;
import io.lettuce.core.codec.RedisCodec;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class RedisProtoCodec<V extends GeneratedMessage> implements RedisCodec<String, V> {
    private final Function<ByteBuffer, V> deserializer;

    public RedisProtoCodec(Class<V> protoType) {
        this.deserializer = getDeserializer(protoType);
    }

    private Function<ByteBuffer, V> getDeserializer(Class<V> protoType) {
        try {
            var lookup = MethodHandles.lookup();
            var type = MethodType.methodType(protoType, java.nio.ByteBuffer.class);
            var method = lookup.findStatic(
                protoType, "parseFrom", type
            );
            return it -> {
                try {
                    return protoType.cast(method.invoke(it));
                } catch (Throwable e) {
                    log.error("error parsing proto {}", e.getMessage());
                    throw new ProtoSerializationException(e);
                }
            };
        } catch (IllegalAccessException | NoSuchMethodException e) {
            //will never happen
            log.fatal("Unexpected proto error {}", e.getMessage());
            throw new ProtoSerializationException(e);
        }
    }

    @Override
    public String decodeKey(ByteBuffer byteBuffer) {
        return new String(byteBuffer.array(), StandardCharsets.UTF_8);
    }

    @Override
    public V decodeValue(ByteBuffer byteBuffer) {
        return deserializer.apply(byteBuffer);
    }

    @Override
    public ByteBuffer encodeKey(String s) {
        if (s == null) {
            return ByteBuffer.wrap(new byte[0]);
        }
        return ByteBuffer.wrap(s.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public ByteBuffer encodeValue(V v) {
        if (v == null) {
            return ByteBuffer.wrap(new byte[0]);
        }
        return ByteBuffer.wrap(v.toByteArray());
    }

    public static class ProtoSerializationException extends RuntimeException {
        public ProtoSerializationException(Throwable cause) {
            super(cause);
        }
    }
}
