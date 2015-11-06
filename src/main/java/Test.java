import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by artemvlasov on 12/06/15.
 */
public class Test {
    public static void main(String... args) {
        Pattern pattern = Pattern.compile(".?полезняшк.?", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        Matcher matcher = pattern.matcher("_Полезняшка");
        System.out.println(matcher.matches());
    }
}
