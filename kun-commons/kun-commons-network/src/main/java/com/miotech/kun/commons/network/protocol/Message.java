package com.miotech.kun.commons.network.protocol;

import com.miotech.kun.commons.network.buffer.ManagedBuffer;
import io.netty.buffer.ByteBuf;

public interface Message extends Encodable {
    /** Used to identify this request type. */
    Type type();

    /** An optional body for the message. */
    ManagedBuffer body();

    /** Whether to include the body of the message in the same frame as the message. */
    boolean isBodyInFrame();

    /** Preceding every serialized Message is its type, which allows us to deserialize it. */
    enum Type implements Encodable {
        RpcRequest(0), RpcResponse(1), RpcFailure(2);

        private final byte id;

        Type(int id) {
            assert id < 128 : "Cannot have more than 128 message types";
            this.id = (byte) id;
        }

        public byte id() { return id; }

        @Override public int encodedLength() { return 1; }

        @Override public void encode(ByteBuf buf) { buf.writeByte(id); }

        public static Type decode(ByteBuf buf) {
            byte id = buf.readByte();
            switch (id) {
                case 0: return RpcRequest;
                case 1: return RpcResponse;
                case 2: return RpcFailure;
                case -1: throw new IllegalArgumentException("User type messages cannot be decoded.");
                default: throw new IllegalArgumentException("Unknown message type: " + id);
            }
        }
    }
}
