public class BusTicket {

    private int id;
    private String destination;
    private double price;
    private boolean available;

    public BusTicket(int id, String destination, double price, boolean available) {
        this.id = id;
        this.destination = destination;
        this.price = price;
        this.available = available;
    }
//Сеттеры, геттеры, чтобы можно было оставить поля приватными
    public int getId() { return id; }
    public String getDestination() { return destination; }
    public double getPrice() { return price; }
    public boolean isAvailable() { return available; }

    public void setDestination(String destination) { this.destination = destination; }
    public void setPrice(double price) { this.price = price; }
    public void setAvailable(boolean available) { this.available = available; }

    @Override
    public String toString() { //преобразовывает данные в строку для записи в файл
        return id + "|" + destination + "|" + price + "|" + available;
    }

    public static BusTicket fromString(String line) { //функция считывает строку из файла
        String[] p = line.split("\\|");
        return new BusTicket(
                Integer.parseInt(p[0]),
                p[1],
                Double.parseDouble(p[2]),
                Boolean.parseBoolean(p[3])
        );
    }
}
