package com.miotech.kun.commons.utils;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class FtlUtils {

    private static Logger logger = LoggerFactory.getLogger(FtlUtils.class);


    public static void processTemplate(Class<?> resourceLoaderClass, String ftlFile, String output, Map<String, Object> map) {
        try {
            Configuration config = new Configuration(Configuration.getVersion());
            config.setTemplateLoader(new ClassTemplateLoader(resourceLoaderClass, "/"));
            Template template = config.getTemplate(ftlFile);
            template.process(map, new FileWriter(output));
        } catch (IOException | TemplateException e) {
            logger.error("process template failed", e);
        }
    }

}
