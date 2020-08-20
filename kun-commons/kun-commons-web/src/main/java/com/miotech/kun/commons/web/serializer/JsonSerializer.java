package com.miotech.kun.commons.web.serializer;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.inject.Singleton;
import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.commons.web.exception.UnhandledTypeException;
import org.eclipse.jetty.http.MimeTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.TimeZone;

@Singleton
public class JsonSerializer {

    private final Logger logger = LoggerFactory.getLogger(JsonSerializer.class);
    private static ObjectMapper objectMapper;

    private void logDeserializeError(IOException e) {
        logger.error("Failed to deserialize object: ", e);
    }

    static {
        objectMapper = new ObjectMapper();
        // allow empty field
        objectMapper.setVisibility(PropertyAccessor.FIELD,
                JsonAutoDetect.Visibility.ANY);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // default serialize datetime as iso date
        objectMapper.registerModule(new JavaTimeModule());
        SimpleModule module = new SimpleModule();
        module.addSerializer(OffsetDateTime.class, new com.fasterxml.jackson.databind.JsonSerializer<OffsetDateTime>() {
            @Override
            public void serialize(OffsetDateTime offsetDateTime, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
                jsonGenerator.writeString(DateTimeUtils.ISO_DATETIME_NANO_DATETIME_FORMATTER
                        .format(offsetDateTime));
            }
        });
        objectMapper.registerModule(module);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.setDateFormat(new StdDateFormat().withTimeZone(TimeZone.getDefault()).withColonInTimeZone(true));
    }

    public String toString(Object object) {
        try {
           return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize object: ", e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    public <T> T toObject(InputStream inputStream, Type type) {
        try {
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                return objectMapper.readValue(inputStream,
                        objectMapper.getTypeFactory().constructCollectionType(List.class,
                                (Class) actualTypeArguments[0]));

            } else if (type instanceof Class) {
                return (T) objectMapper.readValue(inputStream, (Class) type);
            } else {
                throw new UnhandledTypeException("Unhandled Type: " + type.getTypeName());
            }
        } catch (IOException e) {
            logDeserializeError(e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    public <T> T toObject(String str, Class<T> clz) {
        try {
            return objectMapper.readValue(str, clz);
        } catch (IOException e) {
            logDeserializeError(e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    public <T> T toObject(String str, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(str, typeReference);
        } catch (IOException e) {
            logDeserializeError(e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    public void writeResponseAsJson(HttpServletResponse resp, Object object) {
        PrintWriter out = null;
        try {
            resp.setCharacterEncoding(MimeTypes.Type.APPLICATION_JSON_UTF_8.getCharsetString());
            resp.setContentType(MimeTypes.Type.APPLICATION_JSON_UTF_8.toString());
            out = resp.getWriter();
        } catch (IOException e) {
            logger.error("error in write response \"{}\" as json: ", object, e);
            throw ExceptionUtils.wrapIfChecked(e);
        }

        String content = toString(object);
        out.print(content);
        out.flush();
    }
}
