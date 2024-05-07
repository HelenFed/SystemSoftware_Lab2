import lombok.Data;

@Data
public class File {
    private String name;
    private Descriptor descriptor;

    public File(String name, Descriptor descriptor) {
        this.name = name;               // Ім'я файлу
        this.descriptor = descriptor;   // Дескриптор файлу
    }
}