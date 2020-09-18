package app.base.exceptions;

import app.base.constants.GErrors;
import app.base.objects.GPair;
import app.base.objects.IGEnum;

public class GSystemError extends GException {

    public GSystemError() {
        super(GErrors.INTERNAL_ERROR);
    }

    public GSystemError(IGEnum code, GPair... pairs) {
        super(code, pairs);
    }

    public GSystemError(IGEnum code) {
        super(code);
    }

    public GSystemError(IGEnum code, Throwable t) {
        super(code, t);
    }

    public GSystemError(IGEnum code, String description) {
        super(code, description);
    }

    public GSystemError(Throwable t, GPair... pairs) {
        super(GErrors.INTERNAL_ERROR, t, pairs);
    }

}
