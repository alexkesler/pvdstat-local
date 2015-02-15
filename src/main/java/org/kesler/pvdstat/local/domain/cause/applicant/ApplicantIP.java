package org.kesler.pvdstat.local.domain.cause.applicant;

import org.kesler.pvdstat.local.domain.cause.Applicant;

public class ApplicantIP extends Applicant {
    private FL fl;
    private FL repres;

    @Override
    public String getCommonName() {
        return "ИП " + fl.getFIO() + (repres==null?"":" (" + repres.getFIO() + ")");
    }

    @Override
    public Type getType() {
        return Type.IP;
    }
}
