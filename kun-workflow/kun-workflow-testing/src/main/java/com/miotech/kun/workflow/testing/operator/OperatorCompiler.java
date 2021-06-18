package com.miotech.kun.workflow.testing.operator;

import com.miotech.kun.workflow.core.execution.KunOperator;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.SimpleRemapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

public class OperatorCompiler {
    private static final Logger logger = LoggerFactory.getLogger(OperatorCompiler.class);

    private OperatorCompiler() {
    }

    /**
     * 将一个Operator编译成为Jar供测试使用。
     * @param operatorClass Operator的类
     * @param className     重命名后的类名（完整package名）
     * @return
     */
    public static String compileJar(Class<? extends KunOperator> operatorClass, String className) {
        return buildJar(className, getBytesWithClassName(operatorClass, className));
    }

    private static String buildJar(String className, byte[] bytes) {
        File target;
        try {
            target = File.createTempFile("temp_operator", ".jar");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        try (FileOutputStream fout = new FileOutputStream(target);
             JarOutputStream jarOut = new JarOutputStream(fout)) {
            String path = classPathOf(className);
            int i = path.lastIndexOf('/');
            if (i > -1) {
                jarOut.putNextEntry(new ZipEntry(path.substring(0, i + 1)));
            }
            jarOut.putNextEntry(new ZipEntry(path));
            jarOut.write(bytes);
            jarOut.closeEntry();

            jarOut.close();
            fout.close();
            return target.toURI().toURL().toString();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static String compileOperatorJar(String location, Class<? extends KunOperator> operatorClass, String className) {
        return buildOperatorJar(location, className, getBytesWithClassName(operatorClass, className));
    }

    private static String buildOperatorJar(String location, String operatorName, byte[] bytes) {
        File target = new File(location + "/" + operatorName + ".jar");
        try {
            if(!target.getParentFile().exists()){
                target.getParentFile().mkdirs();
            }
            if (target.exists()) {
                target.delete();
            }
            target.createNewFile();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        try (FileOutputStream fout = new FileOutputStream(target);
             JarOutputStream jarOut = new JarOutputStream(fout)) {
            String path = classPathOf(operatorName);
            int i = path.lastIndexOf('/');
            if (i > -1) {
                jarOut.putNextEntry(new ZipEntry(path.substring(0, i + 1)));
            }
            jarOut.putNextEntry(new ZipEntry(path));
            jarOut.write(bytes);
            jarOut.closeEntry();

            jarOut.close();
            fout.close();
            return target.toURI().toURL().toString();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static byte[] getBytesWithClassName(Class<?> clazz, String className) {
        try {
            ClassReader cr = new ClassReader(getByteStream(clazz));
            ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
            ClassVisitor cv = new ClassRemapper(cw, new SimpleRemapper(internalName(clazz.getName()), internalName(className)));
            cr.accept(cv, ClassReader.SKIP_DEBUG);
            return cw.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static InputStream getByteStream(Class<?> clazz) {
        String classAsPath = classPathOf(clazz.getName());
        return clazz.getClassLoader().getResourceAsStream(classAsPath);
    }

    private static String internalName(String typeName) {
        return typeName.replace('.', '/');
    }

    private static String classPathOf(String className) {
        return className.replace('.', '/') + ".class";
    }
}
