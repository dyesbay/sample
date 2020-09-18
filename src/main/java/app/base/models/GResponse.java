package app.base.models;

import app.base.objects.GObject;
import app.base.objects.IGEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.util.Date;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GResponse extends GObject {

    private String code;
    private String message;
    @Builder.Default
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Date date = new Date();
    private Object data;
    private Map<String, String> fields;

    public GResponse(IGEnum error) {
        this.code = error.getKey();
        this.message = error.getValue();
    }
}
