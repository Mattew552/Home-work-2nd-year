import java.io.*;
import java.nio.file.*;
import java.util.*;

/*



createDB(path) - создать файл БД
openDB(path) - открыть файл и построить индекс
addRecord - добавить запись O(1)
searchById(id) - найти запись по id  O(1)
deleteById(id) - удалить запись O(1)
editRecord - редактирование O(1)
backup(path) - копировать db в backup
restore(path) - заменить db backup'ом и перестроить индекс
SearchByDestination(String) - поиск по маршруту за О(1)
DeleteByDestination(String) - удаление по маршруту за О(1), зависит от количества билетов с одинаковым маршрутом
Высокая скорость благодаря hashMap и индексам
 */
public class TicketStorage {

    // Файл с данными
    private File dbFile;

    // Файл индекса (id;offset per line)
    private File idxFile;


    // Доступ к HashMap O(1)
    private final HashMap<Integer, Long> index = new HashMap<>();
    private final HashMap<String, Set<Integer>> destinationIndex = new HashMap<>();


    public TicketStorage() { }

    /*
     Создать новую базу данных
     path — путь к файлу
     */
    public void createDB(String path) throws IOException {
        dbFile = new File(path);
        idxFile = new File(path + ".idx");

        // Убедимся, что родительская директория существует
        File parent = dbFile.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();

        // Если файла не было — создаём; если был — очищаем его
        if (!dbFile.exists()) {
            dbFile.createNewFile();
        } else {
            try (FileOutputStream fos = new FileOutputStream(dbFile, false)) {
            }
        }

        // Сбрасываем индекс и записываем пустой idxFile
        index.clear();
        saveIndex();
    }

    /*
     Открыть существующую БД и построить индекс сканированием.
     buildIndexFromFile() выполняется внутри.
     */
    public void openDB(String path) throws IOException {
        dbFile = new File(path);
        if (!dbFile.exists()) throw new FileNotFoundException("DB file not found: " + path);
        idxFile = new File(path + ".idx");
        buildIndexFromFile();
    }

    /*
     Построить индекс, сканируя файл. Используем RandomAccessFile, чтобы
     корректно получить смещения каждого начала строки.
 */
    private void buildIndexFromFile() throws IOException {
        index.clear();
        destinationIndex.clear();
        try (RandomAccessFile raf = new RandomAccessFile(dbFile, "r")) {
            long fileLen = raf.length();
            long pos = 0;
            while (pos < fileLen) {
                raf.seek(pos);
                String line = raf.readLine(); // строка без '\n'
                if (line == null) break;

                if (line.length() > 0 && line.charAt(0) == '1') {
                    //парсим payload (без флага)
                    String payload = line.substring(1);
                    try {
                        BusTicket t = BusTicket.fromString(payload);
                        index.put(t.getId(), pos);
                        destinationIndex
                                .computeIfAbsent(t.getDestination(), k -> new HashSet<>())
                                .add(t.getId());
                    } catch (Exception ex) {
                        // Некорректная строка - пропускаем
                    }
                }
                // RandomAccessFile.getFilePointer() указывает на начало следующей строки
                pos = raf.getFilePointer();
            }
        }
    }

    /*
     Сохранить текущий индекс в .idx файл.
     Формат .idx: построчно "id;offset"
     Это опция ускоряет последующее открытие.
     */
    public void saveIndex() throws IOException {
        if (dbFile == null) throw new IllegalStateException("DB file not set");
        if (idxFile == null) idxFile = new File(dbFile.getPath() + ".idx");

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(idxFile, false))) {
            for (Map.Entry<Integer, Long> e : index.entrySet()) {
                bw.write(e.getKey() + ";" + e.getValue());
                bw.newLine();
            }
        }
    }

    /*
     Загрузить индекс из .idx файла (если он корректен).
     */
    public void loadIndexFromIdxFile() throws IOException {
        if (dbFile == null) throw new IllegalStateException("DB file not set");
        if (idxFile == null) idxFile = new File(dbFile.getPath() + ".idx");
        if (!idxFile.exists()) throw new FileNotFoundException("Index file not found: " + idxFile.getPath());

        index.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(idxFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length != 2) continue;
                int id = Integer.parseInt(parts[0]);
                long off = Long.parseLong(parts[1]);
                index.put(id, off);
            }
        }
    }

    /*
     Добавить запись. Проверка уникальности через in-memory index -> O(1).
     Возвращает true при успешном добавлении, false если id уже существует.
     */
    public boolean addRecord(BusTicket t) throws IOException {
        if (dbFile == null) throw new IllegalStateException("DB not opened");
        int id = t.getId();
        if (index.containsKey(id)) return false; // уже есть

        String payload = t.toRecordPayload();          // "id|destination|price|available"
        String line = "1" + payload + System.lineSeparator(); // '1' флаг = активна

        try (RandomAccessFile raf = new RandomAccessFile(dbFile, "rw")) {
            long offset = raf.length(); // позиция начала новой строки
            raf.seek(offset);
            raf.write(line.getBytes()); // пишем
            index.put(id, offset);
            // обновляем индекс в памяти
            destinationIndex//Если направление существует, возвращаем его
                    .computeIfAbsent(t.getDestination(), k -> new HashSet<>())
                    .add(t.getId());
        }
        return true;
    }

    /*
     Прочитать все активные записи, используется для отображения в GUI.

     */
    public List<BusTicket> readAll() throws IOException {
        if (dbFile == null) throw new IllegalStateException("DB not opened");
        List<BusTicket> out = new ArrayList<>();
        try (RandomAccessFile raf = new RandomAccessFile(dbFile, "r")) {
            long fileLen = raf.length();
            long pos = 0;
            while (pos < fileLen) {
                raf.seek(pos);
                String line = raf.readLine();
                if (line == null) break;
                if (line.length() > 0 && line.charAt(0) == '1') {
                    String payload = line.substring(1);
                    try {
                        BusTicket t = BusTicket.fromString(payload);
                        out.add(t);
                    } catch (Exception ex) {
                        // игнорируем повреждённую строку
                    }
                }
                pos = raf.getFilePointer();
            }
        }
        return out;
    }

    /*
     Поиск по ключу id O(1)
     Возвращает объект BusTicket или null, если не найден.
     */
    public BusTicket searchById(int id) throws IOException {
        if (dbFile == null) throw new IllegalStateException("DB not opened");
        Long offset = index.get(id);
        if (offset == null) return null;

        try (RandomAccessFile raf = new RandomAccessFile(dbFile, "r")) {
            raf.seek(offset);
            String line = raf.readLine();
            if (line == null || line.length() == 0) return null;
            if (line.charAt(0) != '1') return null; // помечено удалённым
            String payload = line.substring(1);
            return BusTicket.fromString(payload);
        }
    }

    /*
      Удаление по id O(1).
      флаг '1' -> '0' в начале строки.
     Также удаляем запись из index.
     */
    public boolean deleteById(int id) throws IOException {
        if (dbFile == null) throw new IllegalStateException("DB not opened");
        Long offset = index.remove(id);
        if (offset == null) return false;
        String destination=null;
        try (RandomAccessFile raf = new RandomAccessFile(dbFile, "rw")) {
            raf.seek(offset);
            int b = raf.read();
            if (b == -1) return false;
            char flag = (char) b;
            if (flag != '1') return false; // уже удалён
            raf.seek(offset);
            raf.writeByte((byte) '0'); // пометим как удалённую
        }
        index.remove(id);

        if (destination != null) {
            Set<Integer> ids = destinationIndex.get(destination);
            if (ids != null) {
                ids.remove(id);
                if (ids.isEmpty()) {
                    destinationIndex.remove(destination);
                }
            }
        }
        return true;
    }

    /*
     Редактирование записи:
     пометить старую запись '0'
     сделать новую (и обновить индекс)
     это O(1).
     */
    public boolean editRecord(BusTicket updated) throws IOException {
        if (dbFile == null) throw new IllegalStateException("DB not opened");
        int id = updated.getId();
        Long oldOffset = index.get(id);
        if (oldOffset == null) return false;
        String oldDestination=null;
        // пометить старую запись удалённой
        try (RandomAccessFile raf = new RandomAccessFile(dbFile, "rw")) {
            raf.seek(oldOffset);
            String line = raf.readLine();
            if (line == null || line.length() == 0) return false;
            if (line.charAt(0) != '1') return false; // уже удалена

            String payload = line.substring(1);
            BusTicket oldTicket = BusTicket.fromString(payload);
            oldDestination = oldTicket.getDestination();//Сохраняем старое направление

            // Помечаем старую как удаленную
            raf.seek(oldOffset);
            raf.writeByte((byte) '0');
        }

        // Удаляем из старого destinationIndex
        if (oldDestination != null) {
            Set<Integer> oldIds = destinationIndex.get(oldDestination);
            if (oldIds != null) {
                oldIds.remove(id);//Удаляем старое id
                if (oldIds.isEmpty()) {//если множество пустое, удаляем запись из destinationIndex
                    destinationIndex.remove(oldDestination);
                }
            }
        }

        // Удаляем из основного индекса
        index.remove(id);

        // Добавляем новую запись (которая обновит оба индекса)
        return addRecord(updated);
    }

    /*
     Backup: копируем файл dbFile -> backupPath
     */
    public void backup(String backupPath) throws IOException {
        if (dbFile == null) throw new IllegalStateException("DB not opened");
        Files.copy(dbFile.toPath(), Paths.get(backupPath), StandardCopyOption.REPLACE_EXISTING);
    }

    /*
     Restore: копируем backupPath -> dbFile и перестраиваем индекс
     */
    public void restore(String backupPath) throws IOException {
        if (dbFile == null) throw new IllegalStateException("DB not opened");
        Files.copy(Paths.get(backupPath), dbFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        buildIndexFromFile();
    }

    /* Возвращает true если БД открыта и существует */
    public boolean isOpened() {
        return dbFile != null && dbFile.exists();
    }
    public List<BusTicket> searchByDestination(String destination) throws IOException {
        if (dbFile == null) throw new IllegalStateException("DB not opened");

        // Всегда возвращаем List (не null)
        List<BusTicket> results = new ArrayList<>();

        Set<Integer> ids = destinationIndex.get(destination);
        if (ids == null || ids.size() == 0) {
            return results; // возвращаем пустой список
        }

        // Создаем копию множества, чтобы избежать проблем при одновременном изменении
        Set<Integer> idsCopy = new HashSet<>(ids);

        for (int id : idsCopy) {
            BusTicket ticket = searchById(id);
            if (ticket != null) {
                results.add(ticket);
            }
        }

        return results;
    }
    public int deleteByDestination(String destination) throws IOException {
        if (dbFile == null) throw new IllegalStateException("DB not opened");
        //Получаем множество id билетов с указанным направлением
        Set<Integer> ids = destinationIndex.get(destination);
        if (ids == null || ids.isEmpty()) {
            return 0;
        }

        // Создаем копию, чтобы избежать ConcurrentModificationException
        Set<Integer> idsCopy = new HashSet<>(ids);
        int count = 0;
        //Проходим все id, удаляем
        for (int id : idsCopy) {
            if (deleteById(id)) {
                count++;
            }
        }

        return count;
    }
}