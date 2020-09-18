package app.base.db;

import app.base.objects.GObject;

import java.io.Serializable;

public abstract class GEntity<ID extends Serializable> extends GObject {

    public ID getId() {
        return null;
    }

    public boolean isDisabled() {
        return false;
    }

    public String getCacheKey() {
        return getId() == null ? null : getId().toString();

    }

}
