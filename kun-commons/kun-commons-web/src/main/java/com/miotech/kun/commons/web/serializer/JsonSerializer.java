package com.miotech.kun.commons.web.serializer;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.inject.Singleton;
import com.miotech.kun.commons.utils.ExceptionUtils;
import org.eclipse.jetty.http.MimeTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

@Singleton
public class JsonSerializer {

    private final Logger logger = LoggerFactory.getLogger(JsonSerializer.class);
    private static ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        // allow empty field
        objectMapper.setVisibility(PropertyAccessor.FIELD,
                JsonAutoDetect.Visibility.ANY);

        // default serialize datetime as iso date
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.setDateFormat(new StdDateFormat().withColonInTimeZone(true));
    }

    public String toString(Object object) {
        try {
           return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize object: ", e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    public <T> T toObject(InputStream inputStream, Class<T> clz) {
        try {
            return objectMapper.readValue(inputStream, clz);
        } catch (IOException e) {
            logger.error("Failed to deserialize object: ", e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    public <T> T toObject(String str, Class<T> clz) {
        try {
            return objectMapper.readValue(str, clz);
        } catch (IOException e) {
            logger.error("Failed to deserialize object: ", e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    public void writeResponseAsJson(HttpServletResponse resp, Object object) {
        PrintWriter out = null;
        try {
            out = resp.getWriter();
        } catch (IOException e) {
            logger.error("error in write response \"{}\" as json: ", object, e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
        resp.setContentType(MimeTypes.Type.APPLICATION_JSON_UTF_8.toString());
        resp.setCharacterEncoding(MimeTypes.Type.APPLICATION_JSON_UTF_8.getCharsetString());

        String content = toString(object);
        out.print(content);
        out.flush();
    }
}
