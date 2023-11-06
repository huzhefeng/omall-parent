import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

public class testPasswd {


    public static void main(String[] args) {
        String passwd="123";
        String newPasswd = DigestUtils.md5DigestAsHex(passwd.getBytes(StandardCharsets.UTF_8));


        System.out.println(newPasswd);
    }
}
