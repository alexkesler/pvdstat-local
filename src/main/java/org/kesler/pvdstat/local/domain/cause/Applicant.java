package org.kesler.pvdstat.local.domain.cause;

import org.kesler.pvdstat.local.domain.Cause;


public abstract class Applicant {

    protected Cause cause;

    public Cause getCause() {
        return cause;
    }
    public void setCause(Cause cause) {
        this.cause = cause;
    }
    public abstract String getCommonName();
    public abstract Type getType();

    public enum Type {
        FL,
        IP,
        UL
    }

}
