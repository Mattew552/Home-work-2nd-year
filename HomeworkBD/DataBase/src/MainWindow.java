import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MainWindow extends JFrame {//класс главного окна

    private JTable table;//таблица с данными
    private DefaultTableModel tableModel;//модель таблицы
    private TicketStorage storage = new TicketStorage();//переменная для работы с хранилищем

    public MainWindow() {//основное окно

        setTitle("Bus Ticket Database");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//завершение программы при закрытии окна

        //Таблица
        String[] cols = {"ID", "Destination", "Price", "Available"};//заголовки столбцов
        tableModel = new DefaultTableModel(cols, 0);//модель без строк
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);//выставляем по центру

        //Панель кнопок
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton bCreate = new JButton("Create DB");
        JButton bOpen = new JButton("Open DB");
        JButton bAdd = new JButton("Add");
        JButton bDel = new JButton("Delete");
        JButton bSearch = new JButton("Search");
        JButton bEdit = new JButton("Edit");
        JButton bRefresh = new JButton("Synchronize");
        JButton bBackup = new JButton("Backup");
        JButton bRestore = new JButton("Restore");

        panel.add(bCreate); panel.add(bOpen); panel.add(bAdd); panel.add(bDel);
        panel.add(bSearch); panel.add(bEdit); panel.add(bRefresh);
        panel.add(bBackup); panel.add(bRestore);

        add(panel, BorderLayout.SOUTH);

        //Подвязываем функции к кнопкам

        bCreate.addActionListener(e -> {//создание базы данных запускает диалоговое окно
            JFileChooser fc = new JFileChooser();
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    storage.createDB(fc.getSelectedFile().getPath());
                    JOptionPane.showMessageDialog(this, "DB Created");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex);
                }
            }
        });

        bOpen.addActionListener(e -> {//Кнопка открытия БД
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                storage.openDB(fc.getSelectedFile().getPath());
                refreshTable();//обновляем таблицу с данными
            }
        });

        bAdd.addActionListener(e -> {//кнопка добавления записи
            if (!storage.isOpened()) return;//Проверка, открыта ли бд

            JTextField id = new JTextField();//поля для ввода данных
            JTextField dest = new JTextField();
            JTextField price = new JTextField();
            JCheckBox avail = new JCheckBox("Available");

            Object[] fields = {
                    "ID:", id,
                    "Destination:", dest,
                    "Price:", price,
                    avail
            };

            if (JOptionPane.showConfirmDialog(this, fields, "Add", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {//диалог ввода данных
                try {
                    BusTicket t = new BusTicket(//из данных делаем новый билет
                            Integer.parseInt(id.getText()),
                            dest.getText(),
                            Double.parseDouble(price.getText()),
                            avail.isSelected()
                    );
                    if (!storage.addRecord(t)) {//добавляем в базу (искл. если id уже существует)
                        JOptionPane.showMessageDialog(this, "ID exists!");
                    }
                    refreshTable();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex);
                }
            }
        });

        bDel.addActionListener(e -> {//кнопка удаления строки таблицы (по id)
            if (!storage.isOpened()) return;

            String s = JOptionPane.showInputDialog("Enter ID to delete:");
            if (s == null) return;

            try {
                boolean ok = storage.deleteById(Integer.parseInt(s));//Пытаемся удалить
                if (!ok) JOptionPane.showMessageDialog(this, "ID not found");
                refreshTable();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex);
            }
        });

        bSearch.addActionListener(e -> {//поиск по id
            if (!storage.isOpened()) return;

            String s = JOptionPane.showInputDialog("Enter ID:");
            if (s == null) return;//диалог для ввода id

            try {
                BusTicket t = storage.searchById(Integer.parseInt(s));
                if (t == null) {
                    JOptionPane.showMessageDialog(this, "Not found");
                } else {
                    JOptionPane.showMessageDialog(this, t.toString());
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex);
            }
        });

        bEdit.addActionListener(e -> {//кнопка редактирования
            if (!storage.isOpened()) return;

            String s = JOptionPane.showInputDialog("Enter ID to edit:");//диалог для редактирования
            if (s == null) return;

            try {
                BusTicket old = storage.searchById(Integer.parseInt(s));//поиск записи для редактирования
                if (old == null) {
                    JOptionPane.showMessageDialog(this, "Not found");
                    return;
                }
//Поля для ввода с текущими значениями
                JTextField dest = new JTextField(old.getDestination());
                JTextField price = new JTextField(String.valueOf(old.getPrice()));
                JCheckBox avail = new JCheckBox("Available", old.isAvailable());

                Object[] fields = {
                        "Destination:", dest,
                        "Price:", price,
                        avail
                };

                if (JOptionPane.showConfirmDialog(this, fields, "Edit", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                    old.setDestination(dest.getText());//изменяем значения
                    old.setPrice(Double.parseDouble(price.getText()));
                    old.setAvailable(avail.isSelected());

                    storage.editRecord(old);
                    refreshTable();
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex);
            }
        });

        bRefresh.addActionListener(e -> refreshTable());//кнопка синхронизации

        bBackup.addActionListener(e -> {//кнопка бэкапа
            JFileChooser fc = new JFileChooser();
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    storage.backup(fc.getSelectedFile().getPath());//создаем копию по пути
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex);
                }
            }
        });

        bRestore.addActionListener(e -> {//кнопка восстановления из бэкапа
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {//восстанавливаем бд по выбранному пути
                    storage.restore(fc.getSelectedFile().getPath());
                    refreshTable();//Обновляем таблицу
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex);
                }
            }
        });
    }

    private void refreshTable() {//метод обновления данных в таблице
        if (!storage.isOpened()) return;

        tableModel.setRowCount(0);//удаляем все строки

        try {
            List<BusTicket> list = storage.readAll();//получаем записи из базы данных
            for (BusTicket t : list) {
                tableModel.addRow(new Object[]{
                        t.getId(),
                        t.getDestination(),
                        t.getPrice(),
                        t.isAvailable()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e);
        }
    }

    public static void main(String[] args) {//показываем главное окно
        new MainWindow().setVisible(true);
    }
}
