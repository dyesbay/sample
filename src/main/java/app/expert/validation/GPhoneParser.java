package app.expert.validation;

public class GPhoneParser {

    public static String parsePhone(String phone) {
        phone = phone
                .replace(" ", "")
                .replace("-", "")
                .replace("(", "")
                .replace(")", "");
        if (phone.startsWith("8")) {
            phone = phone.replaceFirst("8", "+7");
        }
        return phone;
    }
}
