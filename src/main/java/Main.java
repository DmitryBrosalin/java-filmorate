public class Main {
    public static int increment(int count) {
        return count + 1;
    }
    public static void main (String[] acss) {
        int count = 0;
        while (count < 5) {
            count = increment(count);
        }
        System.out.println(count);
    }
}
