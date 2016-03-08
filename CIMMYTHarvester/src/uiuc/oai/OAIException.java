/*
 * Decompiled with CFR 0_110.
 */
package uiuc.oai;

public class OAIException
extends Exception {
    public short code;
    public static final short FAILED_REFRESH_ERR = 1;
    public static final short HTTP_ERR = 2;
    public static final short ID_ONLY_ERR = 3;
    public static final short INVALID_RESPONSE_ERR = 4;
    public static final short INVALID_VERB_ERR = 5;
    public static final short NO_BASE_URL_ERR = 6;
    public static final short NO_MORE_SETS_ERR = 7;
    public static final short NO_OAI_IDENTIFIER_ERR = 8;
    public static final short NOT_INITIALIZED_ERR = 9;
    public static final short RETRY_AFTER_ERR = 10;
    public static final short RETRY_LIMIT_ERR = 11;
    public static final short UNKNOWN_ELEMENT_ERR = 12;
    public static final short XML_PARSE_ERR = 13;
    public static final short CRITICAL_ERR = 14;
    public static final short OAI_2_ONLY_ERR = 15;
    public static final short OAI_ERR = 15;

    public OAIException(int code, String msg) {
        super(msg);
        this.code = (short)code;
    }
}

