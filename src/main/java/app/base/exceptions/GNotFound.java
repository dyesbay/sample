package app.base.exceptions;

import app.base.constants.GErrors;
import app.base.objects.GPair;
import app.base.objects.IGEnum;

public class GNotFound extends GException {

    public GNotFound() {
        super(GErrors.NOT_FOUND);
    }

    public GNotFound(IGEnum code, GPair... pairs) {
        super(code, pairs);
    }

    public GNotFound(IGEnum code) {
        super(code);
    }

    public GNotFound(IGEnum code, Throwable t) {
        super(code, t);
    }

    public GNotFound(IGEnum code, String description) {
        super(code, description);
    }

    public GNotFound(Throwable t, GPair... pairs) {
        super(GErrors.NOT_FOUND, t, pairs);
    }

}
