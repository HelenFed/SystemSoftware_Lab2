import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Кожен файл, як об’єкт ФС, представлений дескриптором файлу
@Data
public class Descriptor {
    @Getter
    @Setter
    private int id;                // Ідентифікатор файла
    private static int setId = 0;
    private FileType type;         // Тип файлу (звичайний або директорія)
    private int hlink;             // Кількість жорстких посилань на файл
    private int size;              // Розмір файла в байтах
    private List<Block> blocks;    // Мапа номерів блоків файлу

    public Descriptor(FileType type) {
        this.id = setId++;
        this.type = type;
        this.hlink = 1;
        this.size = 0;
        this.blocks = new ArrayList<>();
    }

    // Функція виведення інформації про файл
    @Override
    public String toString() {
        return "id = " + id +
                ", type = " + type +
                ", hlink = " + hlink +
                ", size = " + size +
                ", nblock = " + blocks.size();
    }

    // read fd size - прочитати size байт даних з відкритого файлу, до значення зміщення додається size.
    public byte[] read(int size, int offset) {
        // Індекс блоку з якого починається зчитування
        int blockIndex = offset / FileSystem.BLOCK_SIZE_IN_BYTES;
        // Зміщення у блоці (місце з якого починаємо зчитування у блоці)
        int blockOffset = offset % FileSystem.BLOCK_SIZE_IN_BYTES;
        // Масив-результат запрошеного розміру
        byte[] result = new byte[size];
        int readBytes = 0;
        while(readBytes < size) {
            // Скільки залишилось байтів в блоці
            int remainingBlockSize = FileSystem.BLOCK_SIZE_IN_BYTES - blockOffset;

            // Отримуємо поточний блок, якщо блоку не існує, то він створюється
            Block block = blocks.get(blockIndex);
            if (block == null) {
                block = new Block();;
            }

            // Якщо в блоці більше або рівно стільки байтів, скільки залишилося прочитати,
            // то зчитуємо скільки нам байтів потрібно і виходимо з циклу
            if (remainingBlockSize >= size - readBytes) {
                block.read(result, readBytes, blockOffset, size - readBytes);
                readBytes = size;
            } else {
                // Якщо в блоці залишилось менше байтів, ніж залишилося прочитати,
                // то зчитуємо байти поточного блоку і переходимо на початок наступного
                block.read(result, readBytes, blockOffset, remainingBlockSize);
                readBytes += remainingBlockSize;
                blockIndex++;
                blockOffset = 0;
            }

            // Перевірка чи не вийшли ми за межі кількості блоків у файлі
            if (blockIndex >= blocks.size()) {
                break;
            }
        }
        return result;
    }

    // write fd size – записати size байт даних у відкритий файл, до значення зміщення додається size.
    public boolean write(byte[] data, int offset) {
        // Якщо розмір інформації на запис з якогось місця в файл >= кількості місця в усіх блоках, що складають файл,
        // то повертаємо false
        if (offset + data.length >= blocks.size() * FileSystem.BLOCK_SIZE_IN_BYTES) {
            System.out.println("Not enough space to write");
        } else {
            // Якщо місця вистачає
            int writtenBytes = 0;
            // Індекс блоку з якого починається запис
            int blockIndex = offset / FileSystem.BLOCK_SIZE_IN_BYTES;
            // Зміщення у блоці (місце з якого починаємо запис у блоці)
            int blockOffset = offset % FileSystem.BLOCK_SIZE_IN_BYTES;
            while (writtenBytes < data.length) {
                // Отримуємо поточний блок, блоку не існує, то він створюється
                Block block = blocks.get(blockIndex);
                if (block == null) {
                    blocks.set(blockIndex, new Block());
                    block = blocks.get(blockIndex);
                }

                // Скільки залишилось байтів в блоці
                int remainingBlockSize = FileSystem.BLOCK_SIZE_IN_BYTES - blockOffset;

                // Якщо місця в поточному блоці достатньо, щоб записати кількість байтів, щщо залишилася,
                // то вони записуються і ми виходимо з циклу
                if (remainingBlockSize >= data.length - writtenBytes) {
                    block.write(data, writtenBytes, blockOffset, data.length - writtenBytes);
                    writtenBytes = data.length;
                } else {
                    // Якщо місця в поточному блоці менше, ніж залишилося записати,
                    // то записуємо скільки можемо і переходимо на початок наступного блоку
                    block.write(data, writtenBytes, blockOffset, remainingBlockSize);
                    writtenBytes += remainingBlockSize;
                    blockIndex++;
                    blockOffset = 0;
                }
            }
            return true;
        }
        return false;
    }

    // truncate name size – змінити розмір файлу, на який вказує жорстке посилання з ім’ям name.
    // Якщо розмір файлу збільшується, тоді неініціалізовані дані дорівнюють нулям.
    public void truncate(int size) {
        // Новий розмір файлу у блоках
        int newSizeInBlocks = size / FileSystem.BLOCK_SIZE_IN_BYTES + 1;
        // Якщо кількість блоків була менша, ніж нова, то додаємо недостаючу кількість блоків
        if (blocks.size() < newSizeInBlocks) {
            blocks.addAll(Collections.nCopies(newSizeInBlocks - blocks.size(), null));
        } else if(blocks.size() > newSizeInBlocks) {
            // Якщо кількість блоків стала менше, то виділяємо менший список блоків
            blocks.subList(newSizeInBlocks, blocks.size()).clear();
        }
        // Перевизначаємо параметр розміру
        this.size = size;
    }
}
