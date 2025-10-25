import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class BankDataManager {
    private static final String FILE_NAME = "bank_data.txt";
    private static final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static void saveAccount(BankAccount account) {
        if (account == null) {
            return;
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {
            writer.println(account.toFileString());

            for (Transaction transaction : account.getTransaction()) {
                writer.println(transaction.toFileString());
            }

        } catch (IOException e) {
        }
    }

    public static BankAccount loadAccount() {
        File file = new File(FILE_NAME);

        if (!file.exists()) {
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            String accountNumber = null;
            String ownerName = null;
            double balance = 0;
            List<Transaction> transactions = new ArrayList<>();

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":", 5);

                if (parts[0].equals("ACCOUNT") && parts.length >= 4) {
                    accountNumber = parts[1];
                    ownerName = parts[2];
                    balance = Double.parseDouble(parts[3].replace(',','.'));

                } else if (parts[0].equals("TRANSACTION") && parts.length >= 5) {
                    String type = parts[1];
                    double amount = Double.parseDouble(parts[2].replace(',','.'));
                    LocalDateTime timestamp = LocalDateTime.parse(parts[3], formatter);
                    String description = parts[4];

                    Transaction transaction = new Transaction(type, amount, timestamp, description);
                    transactions.add(transaction);
                }
            }

            if (accountNumber != null && ownerName != null) {
                return new BankAccount(accountNumber, ownerName, balance, transactions);
            }

        } catch (Exception e) {
        }

        return null;
    }
}