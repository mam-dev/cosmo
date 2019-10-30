package org.unitedinternet.cosmo.dav.acl;

public enum AcePrincipalType implements LabeledEnum {
    SELF("self"), PROPERTY("property"), AUTHENTICATED("authenticated"),
    ALL("all"), UNAUTHENTICATED("unauthenticated"), HREF("href");


    private final String label;
    AcePrincipalType(String label) {
        this.label = label;
    }
    @Override
    public String getLabel() {
        return label;
    }
}
