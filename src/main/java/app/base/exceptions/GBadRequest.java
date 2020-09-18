package app.base.exceptions;

import app.base.constants.GErrors;
import app.base.objects.GPair;
import app.base.objects.IGEnum;

public class GBadRequest extends GException {

    public GBadRequest() {
        super(GErrors.BAD_REQUEST);
    }

    public GBadRequest(IGEnum code, GPair... pairs) {
        super(code, pairs);
    }

    public GBadRequest(IGEnum code) {
        super(code);
    }

    public GBadRequest(IGEnum code, Throwable t) {
        super(code, t);
    }

    public GBadRequest(IGEnum code, String description) {
        super(code, description);
    }

    public GBadRequest(Throwable t, GPair... pairs) {
        super(GErrors.BAD_REQUEST, t, pairs);
    }
}
