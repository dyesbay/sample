package app.base.objects;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GPair extends GObject {
    private String key;
    private Object value;

    public static GPair build(IGEnum value) {
        return new GPair(value.name(), value.getValue());
    }

}
