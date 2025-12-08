import java.util.concurrent.Callable;

public class Cook{

    private final String dishName;
    private final int cookingTime;
    private final int CookID;
    public Cook(String dishName, int cookingTime, int CookID){
        this.dishName=dishName;
        this.cookingTime=cookingTime;
        this.CookID=CookID;
    }
    public class CookTask implements Callable<String> {
        @Override
        public String call() throws Exception{
            synchronized (System.out) {
                System.out.println("Повар " + CookID + " начинает готовить " + dishName);
            }
            Thread.sleep(cookingTime);
            synchronized (System.out) {
                System.out.println("Повар " + CookID + " приготовил " + dishName);
            }
            return "Блюдо '" + dishName + "' (повар " + CookID + ") готово";
        }
    }
}
