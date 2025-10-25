import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.*;
public class BankProgram {
    private static BankAccount currentAccount = null;
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("    ДОБРО ПОЖАЛОВАТЬ В БАНКОВСКУЮ СИСТЕМУ    ");
        currentAccount = BankDataManager.loadAccount();
        while (true) {
            showMainMenu();
            int choice = getIntInput("Выберите действие: ");
            try {
                switch (choice) {
                    case 1 -> openAccount();
                    case 2 -> depositMoney();
                    case 3 -> withdrawMoney();
                    case 4 -> showBalance();
                    case 5 -> showTransactions();
                    case 6 -> searchTransactions();
                    case 0 -> {
                        BankDataManager.saveAccount(currentAccount);
                        System.out.println("Выход из программы");
                        return;
                    }
                    default -> System.out.println("Неверный пункт меню");
                }

            } catch (Exception e) {
                System.out.println("Ошибка:" + e.getMessage());
            }
            System.out.println();

        }
    }

    private static void showMainMenu() {
        if (currentAccount == null) {
            System.out.println("\n    ГЛАВНОЕ МЕНЮ    ");
        } else {
            System.out.println("\n    ГЛАВНОЕ МЕНЮ (Счет: " + currentAccount.getAccountNumber() + ")    ");
        }
        System.out.println("1. Открыть счет");
        System.out.println("2. Положить деньги");
        System.out.println("3. Снять деньги");
        System.out.println("4. Показать баланс");
        System.out.println("5. Список транзакций");
        System.out.println("6. Поиск транзакций");
        System.out.println("0. Выход");

    }
    private static void openAccount() {
        System.out.println("\n  Открытие счета");

        if (currentAccount != null) {
            System.out.println("У вас уже есть счет");
            return;
        }
        System.out.print("Введите номер счета: ");
        String accountNumber = scanner.nextLine();

        System.out.print("Введите имя владельца: ");
        String ownerName = scanner.nextLine();

        double initialDeposit = getDoubleInput("Введите начальный депозит: ");

        currentAccount = new BankAccount(accountNumber, ownerName, initialDeposit);
        System.out.println("Счет успешно открыт!");
        BankDataManager.saveAccount(currentAccount);
    }
    private static void depositMoney() {
        checkAccountOpened();

        System.out.println("\n   1 ПОПОЛНЕНИЕ СЧЕТА    ");
        double amount = getDoubleInput("Введите сумму для пополнения: ");
        System.out.print("Введите описание: ");
        String description = scanner.nextLine();

        currentAccount.dodep(amount, description);
        BankDataManager.saveAccount(currentAccount);
    }

    private static void withdrawMoney() {
        checkAccountOpened();

        System.out.println("\n    СНЯТИЕ ДЕНЕГ    ");
        double amount = getDoubleInput("Введите сумму для снятия: ");
        System.out.print("Введите описание: ");
        String description = scanner.nextLine();

        currentAccount.withdraw(amount, description);
        BankDataManager.saveAccount(currentAccount);
    }
    private static void showBalance() {
        checkAccountOpened();
        System.out.println("\n    БАЛАНС СЧЕТА    ");
        System.out.println(currentAccount);
    }

    private static void showTransactions() {
        checkAccountOpened();

        System.out.println("\n    ИСТОРИЯ ТРАНЗАКЦИЙ    ");
        List<Transaction> transactions = currentAccount.getTransaction();

        if (transactions.isEmpty()) {
            System.out.println("Транзакций нет.");
        } else {
            for (int i = 0; i < transactions.size(); i++) {
                System.out.println((i + 1) + ". " + transactions.get(i));
            }
        }
    }
    private static void searchTransactions() {
        checkAccountOpened();

        System.out.println("\n    ПОИСК ТРАНЗАКЦИЙ    ");
        System.out.println("1. По типу (DEPOSIT/WITHDRAW)");
        System.out.println("2. По минимальной сумме");
        System.out.println("3. По описанию");
        int searchType = getIntInput("Выберите тип поиска: ");

        List<Transaction> transactions = currentAccount.getTransaction();
        List<Transaction> results = new ArrayList<>();

        switch (searchType) {
            case 1 -> {
                System.out.print("Введите тип (deposit/withdraw): ");
                String type = scanner.nextLine();
                for (Transaction t : transactions) {
                    if (t.getType().equals(type)) {
                        results.add(t);
                    }
                }
            }
            case 2 -> {
                double minAmount = getDoubleInput("Введите минимальную сумму: ");
                for (Transaction t : transactions) {
                    if (t.getAmount() >= minAmount) {
                        results.add(t);
                    }
                }
            }
            case 3 -> {
                System.out.print("Введите текст для поиска в описании: ");
                String searchText = scanner.nextLine();
                for (Transaction t : transactions) {
                    if (t.getDescription().contains(searchText)) {
                        results.add(t);
                    }
                }
            }
            default -> {
                System.out.println("Неверный тип поиска!");
                return;
            }
        }

        System.out.println("\n--- РЕЗУЛЬТАТЫ ПОИСКА (" + results.size() + " найденно)   ");
        if (results.isEmpty()) {
            System.out.println("Транзакции не найдены.");
        } else {
            for (int i = 0; i < results.size(); i++) {
                System.out.println((i + 1) + ". " + results.get(i));
            }
        }
    }
    private static void checkAccountOpened() {
        if (currentAccount == null) {
            throw new IllegalStateException("Счет не открыт! Сначала откройте счет.");
        }
    }

    private static int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: Введите целое число!");
            }
        }
    }

    private static double getDoubleInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Double.parseDouble(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: Введите число!");
            }
        }
    }


}
