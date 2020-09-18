package app.base.objects;

import app.base.utils.SerializationUtils;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GObject implements Serializable {

    public String toJson() {
        return SerializationUtils.toJson(this);
    }
}
