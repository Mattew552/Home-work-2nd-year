import java.io.*;
import java.nio.file.*;
import java.util.*;

public class TicketStorage { //класс для самой базы данных

    private File dbFile;

    //Создание новой пустой БД
    public void createDB(String path) throws Exception {
        dbFile = new File(path); //создаем файл по указанному пути
        if (!dbFile.exists()) {//проверка на существование файла, если нет, создаем
            dbFile.createNewFile();
        } else {
            new PrintWriter(dbFile).close(); // очистка существующего
        }
    }

    //Открытие существующей БД
    public void openDB(String path) {
        dbFile = new File(path); //сохраняем путь к файлу
    }

    // Добавить запись с проверкой уникальности ID
    public boolean addRecord(BusTicket t) throws Exception { //Далее исключение, на случай, если бд не открыта
        if (dbFile == null) throw new RuntimeException("DB not opened");

        //Проверка уникальности id
        try (BufferedReader br = new BufferedReader(new FileReader(dbFile))) {
            String line;
            while ((line = br.readLine()) != null) {//Читаем построчно
                BusTicket existing = BusTicket.fromString(line);
                if (existing.getId() == t.getId()) return false;//Если id не уникален, возвращаем false
            }
        }

        // Запись
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(dbFile, true))) {
            bw.write(t.toString());//если уникален id, добавляем на новую строку
            bw.newLine();
        }
        return true;
    }

    //Прочитать все записи
    public List<BusTicket> readAll() throws Exception {
        if (dbFile == null) throw new RuntimeException("DB not opened");

        List<BusTicket> list = new ArrayList<>(); //лист для результатов
        try (BufferedReader br = new BufferedReader(new FileReader(dbFile))) {
            String line;//Читаем построчно
            while ((line = br.readLine()) != null) {
                list.add(BusTicket.fromString(line));
            }
        }
        return list;//возвращаем список билетов
    }

    //Удалить по ID
    public boolean deleteById(int id) throws Exception {
        if (dbFile == null) throw new RuntimeException("DB not opened");

        File temp = new File(dbFile.getPath() + ".tmp");//временный файл для перезаписи
        boolean removed = false;

        try (BufferedReader br = new BufferedReader(new FileReader(dbFile));//Читаем исходный файл
             BufferedWriter bw = new BufferedWriter(new FileWriter(temp))) {//Записываем во временный

            String line;
            while ((line = br.readLine()) != null) {
                BusTicket t = BusTicket.fromString(line);
                if (t.getId() == id) {
                    removed = true;
                    continue;
                }
                bw.write(line);
                bw.newLine();
            }
        }

        Files.delete(dbFile.toPath());//Если удалили id, удаляем исходный файл, переименовываем временный
        temp.renameTo(dbFile);

        return removed;
    }

    // Поиск по ID
    public BusTicket searchById(int id) throws Exception {
        if (dbFile == null) throw new RuntimeException("DB not opened");

        try (BufferedReader br = new BufferedReader(new FileReader(dbFile))) {
            String line;
            while ((line = br.readLine()) != null) {//просто читаем построчно, пока не найдем нужный id
                BusTicket t = BusTicket.fromString(line);//выводим всю строку, если нашли id
                if (t.getId() == id) return t;
            }
        }
        return null;
    }

    //Редактирование (замена по ID)
    public boolean editRecord(BusTicket updated) throws Exception {
        if (dbFile == null) throw new RuntimeException("DB not opened");

        File temp = new File(dbFile.getPath() + ".tmp");//временный файл для перезаписи
        boolean changed = false;

        try (BufferedReader br = new BufferedReader(new FileReader(dbFile));//Исходный для чтения, временный для записи
             BufferedWriter bw = new BufferedWriter(new FileWriter(temp))) {

            String line;
            while ((line = br.readLine()) != null) {
                BusTicket t = BusTicket.fromString(line);
                if (t.getId() == updated.getId()) {//если нашли запись по id, меняем
                    bw.write(updated.toString());
                    changed = true; //если меняли, флаг тру, иначе записываем старую строку
                } else {
                    bw.write(line);
                }
                bw.newLine();
            }
        }

        Files.delete(dbFile.toPath());//удаляем исходный файл, переименовываем временный
        temp.renameTo(dbFile);

        return changed;
    }

    // Backup
    public void backup(String backupPath) throws Exception {
        Files.copy(dbFile.toPath(), Paths.get(backupPath), StandardCopyOption.REPLACE_EXISTING);
    }//копируем БД в указанное место

    // Restore
    public void restore(String backupPath) throws Exception {
        Files.copy(Paths.get(backupPath), dbFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }//копируем бекап обратно в БД

    public boolean isOpened() { //Проверка, открыта ли БД
        return dbFile != null;
    }
}

