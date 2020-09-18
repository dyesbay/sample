package app.base.exceptions;

import app.base.constants.GErrors;
import app.base.objects.GPair;
import app.base.objects.IGEnum;

public class GAlreadyExists extends GException {

    public GAlreadyExists() {
        super(GErrors.ALREADY_EXISTS);
    }

    public GAlreadyExists(IGEnum code, GPair... pairs) {
        super(code, pairs);
    }

    public GAlreadyExists(IGEnum code) {
        super(code);
    }

    public GAlreadyExists(IGEnum code, Throwable t) {
        super(code, t);
    }

    public GAlreadyExists(IGEnum code, String description) {
        super(code, description);
    }

    public GAlreadyExists(Throwable t, GPair... pairs) {
        super(GErrors.ALREADY_EXISTS, t, pairs);
    }

}
