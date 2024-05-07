import lombok.Data;

@Data
public class Block {
    private final byte[] content;

    // Блок складається з фіксованої кількості байтів
    public Block() {
        this.content = new byte[FileSystem.BLOCK_SIZE_IN_BYTES];
    }

    // Функція запису розміру size даних з масиву data починаючи з dataOffset у блок (content)
    // починаючи у ньому з місця blockOffset
    public void write(byte[] data, int dataOffset, int blockOffset, int size) {
        System.arraycopy(data, dataOffset, content, blockOffset, size);
    }

    // Функція зчитування розміру size даних з блоку (content) починаючи з blockOffset у масив даних data
    // починаючи у ньому з місця dataOffset
    public void read(byte[] data, int dataOffset, int blockOffset, int size) {
        System.arraycopy(content, blockOffset, data, dataOffset, size);
    }
}