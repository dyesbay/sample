package app.base.exceptions;

import app.base.constants.GErrors;
import app.base.objects.GPair;
import app.base.objects.IGEnum;
import app.base.utils.SerializationUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class GException extends Exception {

    private static final transient IGEnum DEFAULT_CODE = GErrors.INTERNAL_SERVER_ERROR;

    private IGEnum code;

    private String description;

    private String text;

    private List<GPair> pairs;

    public GException(IGEnum code, String description) {
        super(code.getValue());
        this.code = code;
        this.description = description;
    }

    public GException(IGEnum code, GPair... pairs) {
        this(code);
        Map<String, Object> errors = new HashMap<>();

        for (GPair pair : pairs) {
            errors.put(pair.getKey(), pair.getValue());
        }

        description = SerializationUtils.toJson(errors);
    }

    public GException(IGEnum code, String description, Throwable t) {
        super(t);
        this.code = code;
        this.description = description;
    }

    public GException(IGEnum code, Throwable t) {
        super(t);
        this.code = code;
        this.description = t.getMessage();
    }

    public GException(IGEnum code, String description, String text) {
        this(code, description);
        this.text = text;
    }

    public GException(IGEnum code) {
        super(code.getValue());
        this.code = code;
        this.description = code.getValue();
    }

    protected GException(IGEnum code, Throwable throwable, GPair... pairs) {
        super(throwable.getMessage(), throwable);
        this.code = code;
        this.description = code.getValue();
        this.pairs = pairs != null ? new ArrayList<>(Arrays.asList(pairs)) : new ArrayList<>();
    }

}
