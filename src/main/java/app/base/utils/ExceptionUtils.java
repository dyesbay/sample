package app.base.utils;

import app.base.exceptions.GException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ExceptionUtils {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionUtils.class);

    private ExceptionUtils() {
    }

    private static String getStackTraceString(Throwable th, Throwable sourceTh, boolean onlyBasePackage) {
        if (th == null) return "";

        StackTraceElement[] elements = th.getStackTrace();
        int len;

        // Фильтрует повторяющиеся строки. Взято с Throwable.java@printEnclosedStackTrace
        if (sourceTh != null) {
            StackTraceElement[] sourceElements = sourceTh.getStackTrace();
            len = elements.length - 1;
            int sourceLen = sourceElements.length - 1;

            while (len >= 0 && sourceLen >= 0 && elements[len].equals(sourceElements[sourceLen])) {
                len--;
                sourceLen--;
            }
        } else {
            len = elements.length - 1;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <= len; i++) {
            StackTraceElement element = elements[i];
            if ((!onlyBasePackage || element.getClassName().startsWith("hi1.")) && element != null) {
                sb.append("\n\t").append(element);
            }
        }

        return sb.toString();
    }

    private static void appendLine(ByteArrayOutputStream stream, String title, String message) throws IOException {
        stream.write(title.getBytes());
        stream.write((message != null ? message : "null").getBytes());
        stream.write("\n".getBytes());
    }

    private static String getLog(Throwable th, Throwable sourceTh, boolean onlyBasePackage) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            appendLine(stream, "Ex - ", th.getClass().getSimpleName());
            appendLine(stream, "Message: ", th.getMessage());

            if (th instanceof GException) {
                GException hex = (GException) th;

                appendLine(stream, "Code: ", hex.getCode() != null ? hex.getCode().name() : null);
                appendLine(stream, "Description: ", hex.getDescription());
                appendLine(stream, "Text: ", hex.getText());

                if (hex.getPairs() != null) {
                    stream.write("Params: \n".getBytes());
                    hex.getPairs().forEach(pair -> {
                        try {
                            stream.write(" ".getBytes());
                            stream.write(pair.getKey().getBytes());
                            stream.write(": ".getBytes());
                            stream.write(pair.getValue() != null ? pair.getValue().toString().getBytes() : "null".getBytes());
                            stream.write("\n".getBytes());
                        } catch (Exception ex) {
                            // DO NOTHING
                        }
                    });
                }
            }

            appendLine(stream, "Stacktrace: ", getStackTraceString(th, sourceTh, onlyBasePackage));

            if (th.getCause() != null) {
                appendLine(stream, "Caused by: ", ExceptionUtils.getLog(th.getCause(), th, onlyBasePackage));
            }

            return stream.toString();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return "!!! ExceptionUtils.getLog: cannot get result !!!";
        }
    }

    public static String getShortLog(Throwable th) {
        return getLog(th, null, true);
    }

    public static String getFullLog(Throwable th) {
        return getLog(th, null, false);
    }
}
