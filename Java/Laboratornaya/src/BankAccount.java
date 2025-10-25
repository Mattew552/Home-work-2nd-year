import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class BankAccount{
    private String accountNumber;
    private String owner;
    private double balance;
    private List<Transaction> transaction;
    public BankAccount(String accountNumber, String owner, double startAmount){
        if (startAmount<0){
            throw new IllegalArgumentException("Недопустимый начальный депозит");
        }
        this.accountNumber=accountNumber;
        this.owner=owner;
        this.balance=startAmount;
        this.transaction=new ArrayList<>();
        if (startAmount>0){
            this.transaction.add(new Transaction("deposit", startAmount,"Начальная сумма"));
        }
    }
    public BankAccount(String accountNumber, String owner, double balance, List<Transaction> transactions) {
        this.accountNumber = accountNumber;
        this.owner = owner;
        this.balance = balance;
        this.transaction = transactions != null ? transactions : new ArrayList<>();
    }
    public void dodep(double amount, String description){
        if (amount<=0){
            throw new IllegalArgumentException("Ошибка суммы пополнения");
        }
        this.balance +=amount;
        this.transaction.add(new Transaction("deposit", amount, description));
        System.out.println("Пополнение:" + amount+ "руб");

    }
    public void withdraw(double amount, String description){
        if (amount<=0){
            throw new IllegalArgumentException("Ошибка суммы");
        }
        if (amount>balance){
            throw new IllegalArgumentException("Недостаточно средств");
        }
        this.balance-=amount;
        this.transaction.add(new Transaction("withdraw", amount, description));
        System.out.println("Снято:"+amount+"руб");
    }
    public String getAccountNumber(){
        return accountNumber;
    }
    public String getOwner(){
        return owner;
    }
    public double getBalance(){
        return balance;
    }
    public List<Transaction>getTransaction(){
        return transaction;
    }
    public String toString(){
        return String.format("Счет: %s\n Владелец: %s\n Баланс: %.2f руб", accountNumber, owner, balance);
    }
    public String toFileString() {
        return String.format("ACCOUNT:%s:%s:%.2f", accountNumber, owner, balance).replace(',','.');
    }
}
