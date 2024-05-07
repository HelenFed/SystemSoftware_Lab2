import lombok.Data;

@Data
public class OpenFile {
    private File file;
    private int offset;

    public OpenFile(File file) {
        this.file = file;
        this.offset = 0;
    }
}
