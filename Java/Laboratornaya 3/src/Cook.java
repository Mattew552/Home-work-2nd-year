import java.util.concurrent.Callable;

public class Cook implements Runnable{

    private final String dishName;
    private final int cookingTime;
    private final int CookID;
    public Cook(String dishName, int cookingTime, int CookID){
        this.dishName=dishName;
        this.cookingTime=cookingTime;
        this.CookID=CookID;
    }


    @Override
    public void run() {
        try{
            System.out.println("Повар"+CookID+" начинает готовить"+ dishName);
            Thread.sleep(cookingTime);
            System.out.println("Повар"+CookID+" приготовил"+ dishName);

        }
        catch(InterruptedException e){
            Thread.currentThread().interrupt();
            System.out.println("Повар"+CookID+" прерван");
        }

    }
    public static class CookTask implements Callable<String> {
        private final Cook cook;

        public CookTask(Cook cook) {
            this.cook = cook;
        }

        @Override
        public String call() throws Exception {
            cook.run();
            return "Повар " + cook.CookID + " приготовил " + cook.dishName;
        }
    }
}
