import java.security.SecureRandom;
import java.util.*;

public class FileSystem {
    // Розмір блоку файла в байтах
    public final static int BLOCK_SIZE_IN_BYTES = 8;
    // Максимальна довжина імені файла
    public final static int MAX_FILE_NAME_LENGTH = 50;
    // Максимальна кількість числових дескрипторів файлу
    public final static int MAX_NUMBER_OF_NUMERIC_FILE_DESCRIPTORS = 3;
    // Карта відкритих файлів
    private final Map<Integer, OpenFile> openedFiles;
    // Список файлів
    private final List<File> files;

    // Ініціалізація ФС
    public FileSystem() {
        // Ініціалізація карти для відстеження відкритих файлів ФС
        this.openedFiles = new HashMap<>();
        // Для кожного можливого файлового дескриптора додаємо в карту порожнє значення
        for (int i = 0; i < MAX_NUMBER_OF_NUMERIC_FILE_DESCRIPTORS; i++) {
            openedFiles.put(i, null);
        }
        this.files = new ArrayList<>();
    }

    // stat name – вивести інформацію про файл (дані дескриптору файлу)
    public void stat(String name) {
        System.out.println("> stat " + name);
        File file = this.getFile(name);
        // Перевірка чи існує файл з таким ім'ям
        if (file != null) {
            System.out.println(file.getDescriptor().toString());
        } else {
            System.out.println("File not found");
        }
    }

    // ls – вивести список жорстких посилань на файли з номерами дескрипторів файлів в директорії.
    public void ls() {
        System.out.println("> ls");
        for(File file : files) {
            System.out.printf("%s\t\t %s, %s\n",
                    file.getName(),
                    file.getDescriptor().getType(),
                    file.getDescriptor().getId());
        }
    }

    // create name – створити звичайний файл та створити на нього жорстке посилання з ім’ям name у директорії.
    public void create(String name) {
        System.out.println("> create " + name);
        // Перевірка чи відповідає довжина назви файлу заданому ліміту
        if (checkFileNameLength(name)) {
            File foundFile = this.getFile(name);
            // Перевірка чи вже існує файл з таким ім'ям
            if (foundFile != null) {
                System.out.println("File already exists");
            } else {
                // Створення нового об'єкта звичайногго файлу з заданим іменем і дескриптором
                File file = new File(name, new Descriptor(FileType.REGULAR));
                this.files.add(file);
            }
        }
    }

    // fd = open name – відкрити звичайний файл, на який вказує жорстке посилання з ім’ям name.
    public void open(String name) {
        System.out.println("> open " + name);
        File file = getFile(name);
        // Перевірка чи існує файл з таким ім'ям
        if (file == null) {
            System.out.println("File not exist");
        } else {
            int fd = getFd();
            // Перевірка чи була відкрита максимальна кількість файлів
            if (fd != -1) {
                this.openedFiles.put(fd, new OpenFile(file));
                System.out.println("fd = " + fd);
            } else {
                System.out.println("The maximum number of files has been opened");
            }
        }
    }

    // close fd – закрити раніше відкритий файл з числовим дескриптором файлу fd, значення fd стає вільним.
    public void close(int fd) {
        System.out.println("> close " + fd);
        OpenFile openFile = openedFiles.get(fd);
        // Перевірка чи був файл відкритий
        if (openFile == null) {
            System.out.println("File not found");
        } else {
            openedFiles.put(fd, null);
        }
    }

    // seek fd offset – вказати зміщення для відкритого файлу, де почнеться наступне читання або запис
    public void seek(int fd, int offset) {
        System.out.println("> seek " + fd + " " + offset);
        OpenFile openFile = openedFiles.get(fd);
        if (openFile == null) {
            System.out.println("File not found");
        } else {
            // Перевірка порівняння зміщення до розміру файла
            if (openFile.getFile().getDescriptor().getSize() <= offset) {
                System.out.println("Offset too big");
            } else {
                openFile.setOffset(openFile.getOffset() + offset);
            }
        }
    }

    // read fd size – прочитати size байт даних з відкритого файлу, до значення зміщення додається size.
    public void read(int fd, int size) {
        System.out.println("> read " + fd + " " + size);
        OpenFile openFile = openedFiles.get(fd);
        if (openFile == null) {
            System.out.println("File not found");
        } else {
            // Зчитування відбувається за допомогою функції read у файлі Descriptor
            byte[] result = openFile.getFile().getDescriptor().read(size, openFile.getOffset());
            openFile.setOffset(openFile.getOffset() + size);
            System.out.println(Arrays.toString(result));
        }
    }

    // write fd size – записати size байт даних у відкритий файл, до значення зміщення додається size.
    public void write(int fd, int size) {
        System.out.println("> write " + fd + " " + size);
        OpenFile openFile = openedFiles.get(fd);
        if (openFile == null) {
            System.out.println("File not found");
        } else {
            // Запис випадково згенерованого масиву відбувається за допомогою функції write у файлі Descriptor
            if (openFile.getFile().getDescriptor().write(arrayGenerator(size), openFile.getOffset())) {
                openFile.setOffset(openFile.getOffset() + size);
            }
        }
    }

    // link name1 name2 – створити жорстке посилання з ім’ям name2 на файл, на який вказує жорстке посилання з ім’ям name1.
    public void link(String name1, String name2) {
        System.out.println("> link " + name1 + " " + name2);
        if (checkFileNameLength(name2)) {
            File file = getFile(name1);
            if (file == null) {
                System.out.println("File " + name1 + " does not exits");
            } else {
                File file2 = getFile(name2);
                if (file2 != null) {
                    System.out.println("File " + name2 + " already exists");
                } else {
                    files.add(new File(name2, file.getDescriptor()));
                    file.getDescriptor().setHlink(file.getDescriptor().getHlink() + 1);
                }
            }
        }
    }

    // unlink name – знищити жорстке посилання з ім’ям name.
    public void unlink(String name) {
        System.out.println("> unlink " + name);
        File file = getFile(name);
        if (file == null) {
            System.out.println("File " + name + " does not exists");
        } else {
            file.getDescriptor().setHlink(file.getDescriptor().getHlink() - 1);
            files.remove(file);
        }
    }

    // truncate name size – змінити розмір файлу, на який вказує жорстке посилання з ім’ям name.
    public void truncate(String name, int size) {
        System.out.println("> truncate " + name + " " + size);
        File file = getFile(name);
        if (file == null) {
            System.out.println("File " + name + " does not exists");
        } else {
            file.getDescriptor().truncate(size);
        }
    }

    // Знаходження файлу по імені зі списку
    private File getFile(String name) {
        return files.stream()
                .filter(file -> file.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    // Отримання вільного числового файлового дескриптора
    private int getFd() {
        for (Map.Entry<Integer, OpenFile> entry : openedFiles.entrySet()) {
            if (entry.getValue() == null) {
                return entry.getKey();
            }
        }
        return -1;
    }

    // Генератор масиву рандомних значень
    private byte[] arrayGenerator(int size) {
        byte[] randomBytes = new byte[size];
        new SecureRandom().nextBytes(randomBytes);
        return randomBytes;
    }

    // Перевірка довжини імені файла
    private boolean checkFileNameLength(String name) {
        return name.length() >= 1 && name.length() <= MAX_FILE_NAME_LENGTH;
    }
}
