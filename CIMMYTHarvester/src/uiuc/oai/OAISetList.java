/*
 * Decompiled with CFR 0_110.
 * 
 * Could not load the following classes:
 *  org.jdom.Element
 *  org.jdom.JDOMException
 *  org.jdom.Namespace
 */
package uiuc.oai;

import java.util.List;
import java.util.Vector;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import uiuc.oai.OAIException;
import uiuc.oai.OAIRepository;
import uiuc.oai.OAIResumptionStream;
import uiuc.oai.OAISet;
import uiuc.oai.OaiUtil;

public class OAISetList {
    private OAIResumptionStream oaiResume;

    public OAISet getCurrentItem() throws OAIException {
        OAISet s = new OAISet();
        Element node = this.oaiResume.getItem();
        if (node != null) {
            try {
                Element node2 = OaiUtil.getXpathNode("oai:setName", this.getOAIRepository().getNamespaceNode(), node);
                if (node2 != null && node2.getText() != null && !node2.getText().equalsIgnoreCase("")) {
                    s.frndSetSetName(node2.getText());
                }
                if ((node2 = OaiUtil.getXpathNode("oai:setSpec", this.getOAIRepository().getNamespaceNode(), node)) != null && node2.getText() != null && !node2.getText().equalsIgnoreCase("")) {
                    s.frndSetSetSpec(node2.getText());
                }
                List list = OaiUtil.getXpathList("oai:setDescription", this.getOAIRepository().getNamespaceNode(), node);
                s.frndSetSetDescription(list);
                s.frndSetValid(this.isListValid());
            }
            catch (JDOMException te) {
                throw new OAIException(14, te.getMessage());
            }
        } else {
            s = null;
        }
        return s;
    }

    public int getCurrentIndex() throws OAIException {
        return this.oaiResume.getIndex();
    }

    public int getCompleteSize() throws OAIException {
        return this.oaiResume.getCompleteSize();
    }

    public boolean isListValid() {
        return this.oaiResume.isResponseValid();
    }

    public boolean moreItems() throws OAIException {
        return this.oaiResume.more();
    }

    public void moveNext() throws OAIException {
        this.oaiResume.moveNext();
    }

    public OAIRepository getOAIRepository() {
        return this.oaiResume.getRepository();
    }

    public void requery() throws OAIException {
        this.oaiResume.requery();
    }

    protected void frndSetOAIResumptionStream(OAIResumptionStream rs) {
        this.oaiResume = rs;
    }
}

