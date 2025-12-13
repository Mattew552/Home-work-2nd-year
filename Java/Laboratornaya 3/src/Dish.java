public class Dish{
    private final String name;
    private final int cookingTime;
    public Dish(String name, int cookingTime){
        this.name=name;
        this.cookingTime=cookingTime;
    }
    public String getName(){
        return name;

    }
    public int getCookingTime(){
        return cookingTime;
    }
    public String toString(){
        return name+"("+cookingTime+"ms)";
    }


}





