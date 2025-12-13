

// Формат записи в файле(payload): (флаг, есть или удалена запись)id|destination|price|available


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

    // Геттеры и сеттеры
    public int getId() { return id; }
    public String getDestination() { return destination; }
    public double getPrice() { return price; }
    public boolean isAvailable() { return available; }

    public void setDestination(String destination) { this.destination = destination; }
    public void setPrice(double price) { this.price = price; }
    public void setAvailable(boolean available) { this.available = available; }

    /*
     Преобразует payload (формат "id|dest|price|available") в объект BusTicket.

     */
    public static BusTicket fromString(String payload) { //fromString(payload)  — парсит payload (без флага) в объект
        if (payload == null) throw new IllegalArgumentException("payload is null");
        String[] parts = payload.split("\\|", -1);
        if (parts.length != 4) throw new IllegalArgumentException("Invalid payload: " + payload);
        int id = Integer.parseInt(parts[0]);
        String dest = parts[1];
        double price = Double.parseDouble(parts[2]);
        boolean avail = Boolean.parseBoolean(parts[3]);
        return new BusTicket(id, dest, price, avail);
    }

    /*
     Возвращает payload (без флага) в формате "id|dest|price|available".
     Используется при записи в файл (вместе с флагом '1' или '0', флаг понадобится для удаления).
     */
    public String toRecordPayload() {//toRecordPayload()    — возвращает payload (без флага)
        // Не добавляем лишних пробелов — последовательный формат
        return id + "|" + destination + "|" + price + "|" + available;
    }
    @Override
    public String toString() {
        return "BusTicket{id=" + id + ", destination='" + destination + '\'' + ", price=" + price + ", available=" + available + '}';
    }
}