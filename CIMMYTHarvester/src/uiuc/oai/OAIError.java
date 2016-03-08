/*
 * Decompiled with CFR 0_110.
 */
package uiuc.oai;

public class OAIError {
    private String strCode;
    private String strReason;

    protected void frndSetCode(String c) {
        this.strCode = c;
    }

    protected void frndSetReason(String r) {
        this.strReason = r;
    }

    public String getCode() {
        return this.strCode;
    }

    public String getReason() {
        return this.strReason;
    }
}

