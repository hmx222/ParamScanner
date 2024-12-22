import java.time.ZonedDateTime;

public class test {
    public static void main(String[] args) {
        ZonedDateTime timeRequestSent = ZonedDateTime.now();
        System.out.println(timeRequestSent.toLocalDateTime());
    }

}
