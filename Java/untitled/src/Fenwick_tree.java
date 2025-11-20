public class Fenwick_tree<T extends Number>{
    private int[] tree;
    private int n;
    public void build (T[] arr){
        if(arr==null){
            throw new IllegalArgumentException("Массив не может быть с нулевым значением");
        }
        if(arr.length==0){
            throw new IllegalArgumentException("Массив не может быть пустым");
        }
        this.n=arr.length;
        this.tree=new int[n+1];
        for (int i=0;i<n;i++){

            update(i, arr[i].intValue());
        }
    }
    public void update(int index,int delta){
        if(tree==null){
            throw new IllegalArgumentException("Дерево еще не создано, воспользуйтесь build()");
        }
        if (index<0||index>=n){
            throw new IndexOutOfBoundsException("Индекс за границами " + index);
        }
        index+=1;
        while(index<=n){
            tree[index]+=delta;
            index+=index&-index;
        }

    }
    public int prefixSum( int index){
        if(tree==null){
            throw new IllegalArgumentException("Дерево еще не создано, воспользуйтесь build()");
        }
        if (index<0||index>=n){
            throw new IndexOutOfBoundsException("Индекс за границами " + index);
        }
        int sum=0;
        index+=1;
        while(index>0){
            sum+=tree[index];
            index-=index&-index;

        }
        return sum;
    }
    public int rangeSum(int left, int right){
        if(tree==null){
            throw new IllegalArgumentException("Дерево еще не создано, воспользуйтесь build()");
        }
        if(left<0||right<0||left>=n||right>=n){
            throw new IndexOutOfBoundsException("Диапазон за границами");
        }

        if (left>right){
            throw new IllegalArgumentException("Левый индекс не может быть больше правого");
        }
        else{
            return prefixSum(right)-prefixSum(left-1);
        }
    }
    //Доп математика, не участвующая в основной части лабы
    public int getValue(int index){
        if(tree==null){
            throw new IllegalArgumentException("Дерево еще не создано, воспользуйтесь build()");
        }
        if (index<0||index>=n){
            throw new IndexOutOfBoundsException("Индекс за границами " + index);
        }
        int a=prefixSum(index);
        int b=prefixSum(index-1);
        return a-b;
    }
    public void setValue(int index, int value){
        int current=getValue(index);
        int d=value-current;
        update(index, d);
    }







}