/*
 * Decompiled with CFR 0_110.
 */
package uiuc.oai;

public class OAIMetadataFormat {
    private String strMetadataPrefix;
    private String strSchema;
    private String strMetadataNamespace;
    private boolean boolValid = true;

    public boolean isMetadataFormatValid() {
        return this.boolValid;
    }

    protected void frndSetValid(boolean b) {
        this.boolValid = b;
    }

    public String getMetadataNamespace() {
        return this.strMetadataNamespace;
    }

    public String getMetadataPrefix() {
        return this.strMetadataPrefix;
    }

    public String getSchema() {
        return this.strSchema;
    }

    protected void frndSetMetadataNamespace(String mn) {
        this.strMetadataNamespace = mn;
    }

    protected void frndSetMetadataPrefix(String mp) {
        this.strMetadataPrefix = mp;
    }

    protected void frndSetSchema(String s) {
        this.strSchema = s;
    }
}

