package com.miotech.kun.commons.network.serialize;

import java.nio.ByteBuffer;

public interface Serialization {

    <T> ByteBuffer serialize(T  object);

    <T> Object deserialize(ByteBuffer bytes);

    <T> T deserialize(ByteBuffer bytes, ClassLoader classLoader);

}
