package app.base.exceptions;

import app.base.constants.GErrors;
import app.base.objects.GPair;
import app.base.objects.IGEnum;

public class GUnauthorized extends GException {

    public GUnauthorized() {
        super(GErrors.UNAUTHORIZED);
    }

    public GUnauthorized(IGEnum code, GPair... pairs) {
        super(code, pairs);
    }

    public GUnauthorized(IGEnum code) {
        super(code);
    }

    public GUnauthorized(IGEnum code, Throwable t) {
        super(code, t);
    }

    public GUnauthorized(IGEnum code, String description) {
        super(code, description);
    }

    public GUnauthorized(Throwable t, GPair... pairs) {
        super(GErrors.ALREADY_EXISTS, t, pairs);
    }

}
