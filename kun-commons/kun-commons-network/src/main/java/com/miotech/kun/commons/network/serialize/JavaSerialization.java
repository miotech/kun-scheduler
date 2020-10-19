package com.miotech.kun.commons.network.serialize;

import com.miotech.kun.commons.network.utils.ByteBufferInputStream;
import com.miotech.kun.commons.network.utils.ByteBufferOutputStream;
import com.miotech.kun.commons.utils.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.HashMap;

/**
 * A simple serialization implementation using java serialization
 */
public class JavaSerialization implements Serialization {
    private static final Logger logger = LoggerFactory.getLogger(JavaSerialization.class);
    private static final HashMap<String, Class<?>> primClasses
            = new HashMap<>(8, 1.0F);
    static {
        primClasses.put("boolean", boolean.class);
        primClasses.put("byte", byte.class);
        primClasses.put("char", char.class);
        primClasses.put("short", short.class);
        primClasses.put("int", int.class);
        primClasses.put("long", long.class);
        primClasses.put("float", float.class);
        primClasses.put("double", double.class);
        primClasses.put("void", void.class);
    }

    private final ClassLoader defaultLoader;

    public JavaSerialization(ClassLoader loader) {
        this.defaultLoader = loader;
    }

    @Override
    public <T> ByteBuffer serialize(T obj) {
        ByteBufferOutputStream bos = new ByteBufferOutputStream();

        try {
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(obj);
            out.close();
            return bos.toByteBuffer();
        } catch (IOException e) {
            logger.error("", e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    @Override
    public <T> T deserialize(ByteBuffer bytes) {
        ByteBufferInputStream bis = new ByteBufferInputStream(bytes);
        try {
            ObjectInputStream in = deserializeStream(bis, defaultLoader);
            return (T) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    @Override
    public <T> T deserialize(ByteBuffer bytes, ClassLoader classLoader) {
        ByteBufferInputStream bis = new ByteBufferInputStream(bytes);
        try {
            ObjectInputStream in = deserializeStream(bis, classLoader);
            return (T) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    private ObjectInputStream deserializeStream(ByteBufferInputStream bis, ClassLoader classLoader) throws IOException {
        // override the classloader loading logic, using customized classloader
        return new ObjectInputStream(bis) {
            @Override protected Class<?> resolveClass(ObjectStreamClass desc)
                    throws ClassNotFoundException
            {
                String name = desc.getName();
                try {
                    return Class.forName(name, false, classLoader);
                } catch (ClassNotFoundException ex) {
                    Class<?> cl = primClasses.get(name);
                    if (cl != null) {
                        return cl;
                    } else {
                        throw ex;
                    }
                }
            }
        };
    }
}
