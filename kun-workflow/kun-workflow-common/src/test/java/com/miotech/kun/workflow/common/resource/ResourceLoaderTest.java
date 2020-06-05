package com.miotech.kun.workflow.common.resource;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.*;

import static org.junit.Assert.*;

public class ResourceLoaderTest {

    private ResourceLoader resourceLoader = new ResourceLoaderImpl();

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test(expected = ResourceNotFoundException.class)
    public void getResource_notFound() throws IOException {
        File file = tempFolder.newFile("xyz");
        file.delete();
        resourceLoader.getResource("file://" + file.getPath());
    }

    @Test
    public void getResource_default() throws IOException {
        File file = tempFolder.newFile("xyz");
        Resource resource = resourceLoader.getResource(file.getPath());
        assertNotNull(resource);
    }

    @Test
    public void test_getResource_withCreateParent() throws IOException {
        File dir = tempFolder.newFolder("xyz");
        dir.delete();
        String path = "file://" + dir.getPath() + "/1/2";
        Resource resource = resourceLoader.getResource(path, true);
        assertNotNull(resource);
    }

    @Test
    public void test_getResource_withCreate() throws IOException {
        File file = tempFolder.newFile("xyz");
        file.delete();
        String path = "file://" + file.getPath();
        Resource resource = resourceLoader.getResource(path, true);
        assertEquals(path, resource.getLocation());
        Writer writer= new PrintWriter(resource.getOutputStream());

        String testStr = "hello world";
        writer.write(testStr);
        writer.flush();

        InputStreamReader reader = new InputStreamReader(resource.getInputStream());
        assertEquals(testStr, new BufferedReader(reader).readLine());
    }
}