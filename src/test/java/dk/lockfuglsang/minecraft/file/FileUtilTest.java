package dk.lockfuglsang.minecraft.file;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;

/**
 * JUnit tests for FileUtil
 */
public class FileUtilTest {
    @Test
    public void testGetExtension() {
        assertThat(FileUtil.getExtension("basename.ext"), is("ext"));
        assertThat(FileUtil.getExtension("my file.with.dot.yml"), is("yml"));
    }

    @Test
    public void testBaseName() {
        assertThat(FileUtil.getBasename("dir/something/filename.txt"), is("filename"));
        assertThat(FileUtil.getBasename("dir\\something\\filename.txt"), is("filename"));
    }
}