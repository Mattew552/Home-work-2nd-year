import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;

/*

 Поддерживает:
 Create DB
 Open DB
 Add Ticket
 Delete Ticket (по ID, по destination)
 Search (по ID, по destination)
 Edit (по ID)
 Refresh (перечитать все записи)
 Backup / Restore

 */
public class MainWindow extends JFrame {

    private final DefaultTableModel tableModel;
    private final JTable table;
    private final TicketStorage storage = new TicketStorage();

    public MainWindow() {
        super("Bus Ticket Database");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);

        // Создаём таблицу с колонками
        String[] cols = {"ID", "Destination", "Price", "Available"};
        tableModel = new DefaultTableModel(cols, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {//запрещаем редактирование прямо в таблице
                return false;
            }
        };
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Кнопки внизу
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton bCreate = new JButton("Create DB");
        JButton bOpen = new JButton("Open DB");
        JButton bAdd = new JButton("Add");
        JButton bDelete = new JButton("Delete");
        JButton bSearch = new JButton("Search");
        JButton bEdit = new JButton("Edit");
        JButton bRefresh = new JButton("Refresh");
        JButton bBackup = new JButton("Backup");
        JButton bRestore = new JButton("Restore");
        JButton bSearchDest = new JButton("Search by Dest");
        JButton bDeleteDest = new JButton("Delete by Dest");
        panel.add(bCreate);
        panel.add(bOpen);
        panel.add(bAdd);
        panel.add(bDelete);
        panel.add(bSearch);
        panel.add(bEdit);
        panel.add(bRefresh);
        panel.add(bBackup);
        panel.add(bRestore);
        panel.add(bSearchDest);
        panel.add(bDeleteDest);
        add(panel, BorderLayout.SOUTH);

        //Обработчики кнопок

        // Create DB
        bCreate.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Create DB file");
            int res = fc.showSaveDialog(this);
            if (res == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                try {
                    storage.createDB(f.getAbsolutePath());
                    JOptionPane.showMessageDialog(this, "DB created: " + f.getAbsolutePath());
                    refreshTable(); // показать (пустую) таблицу
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error creating DB: " + ex.getMessage());
                }
            }
        });

        // Open DB
        bOpen.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Open DB file");
            int res = fc.showOpenDialog(this);
            if (res == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                try {
                    storage.openDB(f.getAbsolutePath());
                    JOptionPane.showMessageDialog(this, "DB opened: " + f.getAbsolutePath());
                    refreshTable();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error opening DB: " + ex.getMessage());
                }
            }
        });

        // Add
        bAdd.addActionListener(e -> {
            if (!storage.isOpened()) {
                JOptionPane.showMessageDialog(this, "Open or create DB first");
                return;
            }

            JTextField idField = new JTextField();
            JTextField destField = new JTextField();
            JTextField priceField = new JTextField();
            JCheckBox availCheck = new JCheckBox("Available", true);

            Object[] inputs = {
                    "ID (integer):", idField,
                    "Destination:", destField,
                    "Price (number):", priceField,
                    availCheck
            };

            int ok = JOptionPane.showConfirmDialog(this, inputs, "Add Ticket", JOptionPane.OK_CANCEL_OPTION);
            if (ok == JOptionPane.OK_OPTION) {
                try {
                    int id = Integer.parseInt(idField.getText().trim());
                    String dest = destField.getText().trim();
                    double price = Double.parseDouble(priceField.getText().trim());
                    boolean avail = availCheck.isSelected();

                    BusTicket t = new BusTicket(id, dest, price, avail);
                    boolean added = storage.addRecord(t);
                    if (!added) {
                        JOptionPane.showMessageDialog(this, "ID already exists");
                    } else {
                        refreshTable();
                    }
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(this, "Invalid number: " + nfe.getMessage());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error adding ticket: " + ex.getMessage());
                }
            }
        });

        // Delete by ID
        bDelete.addActionListener(e -> {
            if (!storage.isOpened()) {
                JOptionPane.showMessageDialog(this, "Open or create DB first");
                return;
            }
            String s = JOptionPane.showInputDialog(this, "Enter ID to delete:");
            if (s == null) return;
            try {
                int id = Integer.parseInt(s.trim());
                boolean okDelete = storage.deleteById(id);
                if (!okDelete) {
                    JOptionPane.showMessageDialog(this, "ID not found or already deleted");
                } else {
                    refreshTable();
                }
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "Invalid ID: " + nfe.getMessage());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error deleting: " + ex.getMessage());
            }
        });

        // Search by ID
        bSearch.addActionListener(e -> {
            if (!storage.isOpened()) {
                JOptionPane.showMessageDialog(this, "Open or create DB first");
                return;
            }
            String s = JOptionPane.showInputDialog(this, "Enter ID to search:");
            if (s == null) return;
            try {
                int id = Integer.parseInt(s.trim());
                BusTicket t = storage.searchById(id);
                if (t == null) {
                    JOptionPane.showMessageDialog(this, "Not found");
                } else {
                    JOptionPane.showMessageDialog(this, t.toString());
                }
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "Invalid ID: " + nfe.getMessage());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error searching: " + ex.getMessage());
            }
        });

        // Edit by ID
        bEdit.addActionListener(e -> {
            if (!storage.isOpened()) {
                JOptionPane.showMessageDialog(this, "Open or create DB first");
                return;
            }
            String s = JOptionPane.showInputDialog(this, "Enter ID to edit:");
            if (s == null) return;
            try {
                int id = Integer.parseInt(s.trim());
                BusTicket old = storage.searchById(id);
                if (old == null) {
                    JOptionPane.showMessageDialog(this, "ID not found");
                    return;
                }

                JTextField destField = new JTextField(old.getDestination());
                JTextField priceField = new JTextField(String.valueOf(old.getPrice()));
                JCheckBox availCheck = new JCheckBox("Available", old.isAvailable());

                Object[] inputs = {
                        "Destination:", destField,
                        "Price:", priceField,
                        availCheck
                };

                int ok = JOptionPane.showConfirmDialog(this, inputs, "Edit Ticket ID " + id, JOptionPane.OK_CANCEL_OPTION);
                if (ok == JOptionPane.OK_OPTION) {
                    String dest = destField.getText().trim();
                    double price = Double.parseDouble(priceField.getText().trim());
                    boolean avail = availCheck.isSelected();

                    BusTicket updated = new BusTicket(id, dest, price, avail);
                    boolean updatedOk = storage.editRecord(updated);
                    if (!updatedOk) {
                        JOptionPane.showMessageDialog(this, "Failed to edit (maybe deleted meanwhile)");
                    } else {
                        refreshTable();
                    }
                }
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "Invalid number: " + nfe.getMessage());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error editing: " + ex.getMessage());
            }
        });

        // Refresh
        bRefresh.addActionListener(e -> refreshTable());

        // Backup
        bBackup.addActionListener(e -> {
            if (!storage.isOpened()) {
                JOptionPane.showMessageDialog(this, "Open or create DB first");
                return;
            }
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Save backup to");
            int res = fc.showSaveDialog(this);
            if (res == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                try {
                    storage.backup(f.getAbsolutePath());
                    JOptionPane.showMessageDialog(this, "Backup saved to " + f.getAbsolutePath());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Backup error: " + ex.getMessage());
                }
            }
        });





        bSearchDest.addActionListener(e -> {
            try {
                if (!storage.isOpened()) {
                    JOptionPane.showMessageDialog(this, "Open or create DB first");
                    return;
                }

                String dest = JOptionPane.showInputDialog("Enter destination:");
                if (dest == null || dest.trim().length() == 0) return;

                java.util.List<BusTicket> list = storage.searchByDestination(dest.trim());


                if (list == null) {
                    JOptionPane.showMessageDialog(this, "No tickets found");
                    return;
                }

                int size = list.size();
                if (size == 0) {
                    JOptionPane.showMessageDialog(this, "No tickets found for: " + dest);
                    return;
                }


                StringBuilder sb = new StringBuilder();
                sb.append("Found ").append(size).append(" tickets:\n\n");
                for (BusTicket t : list) {
                    sb.append(t.toString()).append("\n");
                }
                JOptionPane.showMessageDialog(this, sb.toString());

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        bDeleteDest.addActionListener(e -> {
            try {
                if (!storage.isOpened()) {
                    JOptionPane.showMessageDialog(this, "Open or create DB first");
                    return;
                }

                String dest = JOptionPane.showInputDialog("Enter destination to delete:");
                if (dest == null || dest.trim().length() == 0) return;

                // Спросим подтверждение
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Delete ALL tickets to " + dest + "?", "Confirm",
                        JOptionPane.YES_NO_OPTION);

                if (confirm != JOptionPane.YES_OPTION) return;

                int deleted = storage.deleteByDestination(dest.trim());
                JOptionPane.showMessageDialog(this,
                        "Deleted " + deleted + " tickets for destination: " + dest);

                refreshTable();

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        // Restore
        bRestore.addActionListener(e -> {
            if (!storage.isOpened()) {
                JOptionPane.showMessageDialog(this, "Open or create DB first");
                return;
            }
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Select backup file to restore from");
            int res = fc.showOpenDialog(this);
            if (res == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                try {
                    storage.restore(f.getAbsolutePath());
                    JOptionPane.showMessageDialog(this, "Restored from " + f.getAbsolutePath());
                    refreshTable();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Restore error: " + ex.getMessage());
                }
            }
        });

        // Показать окно
        setVisible(true);
    }

    /*
     * Перечитывает все активные записи из файла и обновляет таблицу.
     * Использует storage.readAll() — O(n).
     */
    private void refreshTable() {
        tableModel.setRowCount(0);
        if (!storage.isOpened()) return;
        try {
            for (BusTicket t : storage.readAll()) {
                tableModel.addRow(new Object[]{
                        t.getId(),
                        t.getDestination(),
                        t.getPrice(),
                        t.isAvailable()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error refreshing: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainWindow::new);
    }
}

