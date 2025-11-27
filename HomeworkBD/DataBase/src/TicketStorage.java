import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.io.EOFException;
public class TicketStorage { //класс для самой базы данных

    private File dbFile;
    private Map<Integer, Long> idIndex;
    private Map<String, Set<Integer>> destinationIndex;
    private RandomAccessFile raf;
    public TicketStorage(){
        this.idIndex=new HashMap<>();
        this.destinationIndex=new HashMap<>();
    }

    //Создание новой пустой БД
    public void createDB(String path) throws Exception {
        dbFile = new File(path); //создаем файл по указанному пути
        if (!dbFile.exists()) {//проверка на существование файла, если нет, создаем
            dbFile.createNewFile();
        } else {
            new PrintWriter(dbFile).close(); // очистка существующего
        }
        // Открываем файл для произвольного доступа
        if (raf != null) {
            raf.close();
        }
        raf = new RandomAccessFile(dbFile, "rw");
        idIndex.clear();
        destinationIndex.clear();
    }

    //Открытие существующей БД
    public void openDB(String path) throws Exception {
        dbFile = new File(path); //сохраняем путь к файлу
        // Открываем файл для произвольного доступа
        if (raf != null) {
            raf.close();
        }
        raf = new RandomAccessFile(dbFile, "rw");
        // Перестраиваем индексы при открытии
        rebuildIndexes();
    }
    // Перестроение индексов из файла
    private void rebuildIndexes() throws Exception {
        idIndex.clear();
        destinationIndex.clear();

        if (raf.length() == 0) return;

        raf.seek(0); // Перемещаемся в начало файла
        while (raf.getFilePointer() < raf.length()) {
            long position = raf.getFilePointer(); // Запоминаем текущую позицию
            BusTicket ticket = readNextTicket(); // Читаем следующий билет
            if (ticket != null) {
                // Обновляем индексы
                idIndex.put(ticket.getId(), position);
                destinationIndex.computeIfAbsent(ticket.getDestination(),
                        k -> new HashSet<>()).add(ticket.getId());
            }
        }
    }
    // Чтение следующего билета из текущей позиции
    private BusTicket readNextTicket() throws IOException {
        try {
            // Читаем данные в бинарном формате
            int id = raf.readInt();
            int destLength = raf.readInt();
            byte[] destBytes = new byte[destLength];
            raf.readFully(destBytes);
            String destination = new String(destBytes, "UTF-8"); // ЗАМЕНА: убрали StandardCharsets.
            double price = raf.readDouble();
            boolean available = raf.readBoolean();

            return new BusTicket(id, destination, price, available);
        } catch (EOFException e) {
            return null; // Достигнут конец файла
        }
    }

    // Запись билета в указанную позицию
    private void writeTicket(BusTicket ticket, long position) throws IOException {
        raf.seek(position); // Перемещаемся в нужную позицию
        // Записываем данные в бинарном формате
        raf.writeInt(ticket.getId());
        byte[] destBytes = ticket.getDestination().getBytes("UTF-8"); // ЗАМЕНА: убрали StandardCharsets.
        raf.writeInt(destBytes.length); // Длина строки назначения
        raf.write(destBytes); // Сама строка
        raf.writeDouble(ticket.getPrice());
        raf.writeBoolean(ticket.isAvailable());
    }


    // Добавить запись с проверкой уникальности ID - O(1)
    public boolean addRecord(BusTicket t) throws Exception {
        if (dbFile == null) throw new RuntimeException("DB not opened");
        if (idIndex.containsKey(t.getId())) return false; // Быстрая проверка уникальности через индекс

        // Записываем в конец файла
        long position = raf.length();
        writeTicket(t, position);

        // Обновляем индексы - O(1)
        idIndex.put(t.getId(), position);
        destinationIndex.computeIfAbsent(t.getDestination(),
                k -> new HashSet<>()).add(t.getId());

        return true;
    }

    //Прочитать все записи
    public List<BusTicket> readAll() throws Exception {
        if (dbFile == null) throw new RuntimeException("DB not opened");

        List<BusTicket> list = new ArrayList<>(); //лист для результатов
        for (Map.Entry<Integer, Long> entry : idIndex.entrySet()) {
            raf.seek(entry.getValue());
            BusTicket ticket = readNextTicket();
            if (ticket != null) {
                list.add(ticket);
            }
        }
        return list;//возвращаем список билетов
    }

    // Удалить по ID - O(1)
    public boolean deleteById(int id) throws Exception {
        if (dbFile == null) throw new RuntimeException("DB not opened");

        Long position = idIndex.get(id);
        if (position == null) return false; // Запись не найдена

        // Находим билет для обновления destinationIndex
        raf.seek(position);
        BusTicket ticket = readNextTicket();
        if (ticket == null) return false;

        // Удаляем из индексов - O(1)
        idIndex.remove(id);
        Set<Integer> destIds = destinationIndex.get(ticket.getDestination());
        if (destIds != null) {
            destIds.remove(id);
            if (destIds.isEmpty()) {
                destinationIndex.remove(ticket.getDestination());
            }
        }


        return true;
    }

    // Поиск по ID
    public BusTicket searchById(int id) throws Exception {
        if (dbFile == null) throw new RuntimeException("DB not opened");

        Long position = idIndex.get(id); // Быстрый поиск через индекс
        if (position == null) return null;

        raf.seek(position);
        return readNextTicket(); // Читаем билет по известной позиции
    }
    // Поиск по направлению - O(1) для поиска + O(k) для чтения результатов
    public List<BusTicket> searchByDestination(String destination) throws Exception {
        if (dbFile == null) throw new RuntimeException("DB not opened");

        Set<Integer> ids = destinationIndex.get(destination); // Быстрый поиск через индекс
        if (ids == null) return new ArrayList<>();

        List<BusTicket> results = new ArrayList<>();
        for (int id : ids) {
            BusTicket ticket = searchById(id); // O(1) для каждого ID
            if (ticket != null) {
                results.add(ticket);
            }
        }
        return results;
    }
    // Удаление по направлению - O(1) для поиска + O(k) для удаления
    public int deleteByDestination(String destination) throws Exception {
        if (dbFile == null) throw new RuntimeException("DB not opened");

        Set<Integer> ids = destinationIndex.get(destination);
        if (ids == null) return 0;

        int count = 0;
        // Создаем копию чтобы избежать ConcurrentModificationException
        Set<Integer> idsCopy = new HashSet<>(ids);
        for (int id : idsCopy) {
            if (deleteById(id)) {
                count++;
            }
        }
        return count;
    }

    // Редактирование (замена по ID) - O(1)
    public boolean editRecord(BusTicket updated) throws Exception {
        if (dbFile == null) throw new RuntimeException("DB not opened");

        // Если ID изменился, проверяем уникальность нового ID
        Long oldPosition = idIndex.get(updated.getId());
        if (oldPosition == null) return false;

        // Удаляем старую запись из индексов
        raf.seek(oldPosition);
        BusTicket oldTicket = readNextTicket();
        if (oldTicket != null) {
            Set<Integer> destIds = destinationIndex.get(oldTicket.getDestination());
            if (destIds != null) {
                destIds.remove(updated.getId());
                if (destIds.isEmpty()) {
                    destinationIndex.remove(oldTicket.getDestination());
                }
            }
        }

        // Записываем обновленную версию в конец файла
        long newPosition = raf.length();
        writeTicket(updated, newPosition);
        // Обновляем индексы
        idIndex.put(updated.getId(), newPosition);
        destinationIndex.computeIfAbsent(updated.getDestination(),
                k -> new HashSet<>()).add(updated.getId());

        return true;
    }

    // Backup
    public void backup(String backupPath) throws Exception {
        Files.copy(dbFile.toPath(), Paths.get(backupPath), StandardCopyOption.REPLACE_EXISTING);
    }//копируем БД в указанное место

    // Restore
    public void restore(String backupPath) throws Exception {
        Files.copy(Paths.get(backupPath), dbFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        // Перестраиваем индексы после восстановления
        if (raf != null) {
            raf.close();
        }
        raf = new RandomAccessFile(dbFile, "rw");
        rebuildIndexes();
    }//копируем бекап обратно в БД

    public boolean isOpened() { //Проверка, открыта ли БД
        return dbFile != null;
    }
    // Закрытие файла при завершении работы
    public void close() throws IOException {
        if (raf != null) {
            raf.close();
        }
    }



}

