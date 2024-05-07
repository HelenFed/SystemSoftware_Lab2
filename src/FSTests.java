import org.junit.jupiter.api.Test;

public class FSTests {
    @Test
    public void stat() {
        FileSystem fileSystem = new FileSystem();
        fileSystem.create("file1");
        fileSystem.stat("file1");
    }

    @Test
    public void ls() {
        FileSystem fileSystem = new FileSystem();
        fileSystem.create("file1");
        fileSystem.create("file2");
        fileSystem.create("file3");
        fileSystem.create("file4.1");
        fileSystem.create("file4.1");
        fileSystem.link("file4.1", "file4.2");
        fileSystem.ls();
    }

    @Test
    public void create() {
        FileSystem fileSystem = new FileSystem();
        fileSystem.create("file");
        fileSystem.stat("file");
    }

    @Test
    public void open() {
        FileSystem fileSystem = new FileSystem();
        fileSystem.create("file1");
        fileSystem.create("file2");
        fileSystem.create("file3");
        fileSystem.create("file4");
        fileSystem.open("file1");
        fileSystem.open("file2");
        fileSystem.open("file3");
        fileSystem.open("file4");
    }

    @Test
    public void close() {
        FileSystem fileSystem = new FileSystem();
        fileSystem.create("file");
        fileSystem.close(0);
        fileSystem.open("file");
        fileSystem.close(0);
    }

    @Test
    public void seek() {
        FileSystem fileSystem = new FileSystem();
        fileSystem.create("file");
        fileSystem.open("file");
        fileSystem.seek(0, 2);
        fileSystem.stat("file");
        fileSystem.truncate("file", 5);
        fileSystem.seek(0, 2);
    }

    @Test
    public void read() {
        FileSystem fileSystem = new FileSystem();
        fileSystem.create("file");
        fileSystem.truncate("file", 21);
        fileSystem.open("file");
        fileSystem.seek(0, 4);
        fileSystem.write(0, 10);
        fileSystem.open("file");
        fileSystem.read(1, 21);
        fileSystem.read(0, 21);
    }

    @Test
    public void write() {
        FileSystem fileSystem = new FileSystem();
        fileSystem.create("file");
        fileSystem.write(0, 10);
        fileSystem.open("file");
        fileSystem.write(0, 10);
        fileSystem.stat("file");
        fileSystem.truncate("file", 11);
        fileSystem.write(0, 10);
        fileSystem.stat("file");
    }

    @Test
    public void link() {
        FileSystem fileSystem = new FileSystem();
        fileSystem.link("file1", "file2");
        fileSystem.create("file1");
        fileSystem.stat("file1");
        fileSystem.link("file1", "file2");
        fileSystem.stat("file1");
        fileSystem.link("file2", "file3");
        fileSystem.stat("file3");
    }

    @Test
    public void unlink() {
        FileSystem fileSystem = new FileSystem();
        fileSystem.create("file1");
        fileSystem.ls();
        fileSystem.link("file1", "file2");
        fileSystem.ls();
        fileSystem.unlink("file1");
        fileSystem.ls();
    }

    @Test
    public void truncate() {
        FileSystem fileSystem = new FileSystem();
        fileSystem.create("file");
        fileSystem.stat("file");
        fileSystem.truncate("file", 9);
        fileSystem.stat("file");
    }
}