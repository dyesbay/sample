package app.base.exceptions;

import app.base.constants.GErrors;
import app.base.objects.GPair;
import app.base.objects.IGEnum;

public class GNotAllowed extends GException {

    public GNotAllowed() {
        super(GErrors.NOT_ALLOWED);
    }

    public GNotAllowed(IGEnum code, GPair... pairs) {
        super(code, pairs);
    }

    public GNotAllowed(IGEnum code) {
        super(code);
    }

    public GNotAllowed(IGEnum code, Throwable t) {
        super(code, t);
    }

    public GNotAllowed(IGEnum code, String description) {
        super(code, description);
    }

    public GNotAllowed(Throwable t, GPair... pairs) {
        super(GErrors.NOT_ALLOWED, t, pairs);
    }

}
