public class Main {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("resolve 0");
        Promise<Integer> zero = Promise.resolve(0);
        Promise<Integer> one = Promise.create(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("reject 1");
            throw new RuntimeException("1");
        });
        Promise<String> hello = Promise.create(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("resolve hello");
            return "hello";
        });
        Promise<String> bye = Promise.create(() -> {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("reject bye");
            return "bye";
        });
        System.out.println("Main thread start sleeping.");
        Thread.sleep(2000);
    }
}
