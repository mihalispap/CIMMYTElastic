/*
 * Decompiled with CFR 0_110.
 */
package uiuc.oai;

import java.util.List;
import org.w3c.dom.Node;
import uiuc.oai.OAIException;

public class OAISet {
    private boolean boolValid = true;
    private String strSetSpec;
    private String strSetName;
    private List ixmlDescriptions;

    public Node getSetDescription() throws OAIException {
        return this.getSetDescription(0);
    }

    public Node getSetDescription(int i) throws OAIException {
        if (this.ixmlDescriptions.size() > 0 && i < this.ixmlDescriptions.size()) {
            return (Node)this.ixmlDescriptions.get(i);
        }
        return null;
    }

    public int getSetDescriptionCount() {
        if (this.ixmlDescriptions == null) {
            return 0;
        }
        return this.ixmlDescriptions.size();
    }

    public boolean isSetValid() {
        return this.boolValid;
    }

    protected void frndSetValid(boolean b) {
        this.boolValid = b;
    }

    protected void frndSetSetSpec(String ss) {
        this.strSetSpec = ss;
    }

    protected void frndSetSetName(String sn) {
        this.strSetName = sn;
    }

    protected void frndSetSetDescription(List nl) {
        this.ixmlDescriptions = nl;
    }

    public String getSetSpec() {
        return this.strSetSpec;
    }

    public String getSetName() {
        return this.strSetName;
    }
}

