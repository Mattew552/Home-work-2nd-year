import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.*;
public class Transaction{
    private String type;
    private double amount;
    private LocalDateTime timestamp;
    private String description;
    public Transaction(String type, double amount, String description){
        this.type=type;
        this.amount=amount;
        this.description=description;
        this.timestamp=LocalDateTime.now();
    }
    public Transaction(String type, double amount, LocalDateTime timestamp, String description) {
        this.type = type;
        this.amount = amount;
        this.timestamp = timestamp; // Время из файла
        this.description = description;
    }
    public String getType(){return type;}
    public double getAmount(){
        return amount;
    }
    public LocalDateTime getTimestamp(){
        return timestamp;
    }
    public String getDescription(){
        return description;
    }
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return String.format("[%s] %s: %.2f руб. - %s",
                timestamp.format(formatter), type, amount, description);
    }
    public String toFileString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return String.format("TRANSACTION:%s:%.2f:%s:%s",
                type, amount, timestamp.format(formatter), description).replace(',','.');
    }
}
