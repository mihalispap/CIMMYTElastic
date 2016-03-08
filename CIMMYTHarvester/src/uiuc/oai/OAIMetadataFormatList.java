/*
 * Decompiled with CFR 0_110.
 * 
 * Could not load the following classes:
 *  org.jdom.Element
 *  org.jdom.JDOMException
 *  org.jdom.Namespace
 */
package uiuc.oai;

import java.util.Vector;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import uiuc.oai.OAIException;
import uiuc.oai.OAIMetadataFormat;
import uiuc.oai.OAIRepository;
import uiuc.oai.OAIResumptionStream;
import uiuc.oai.OaiUtil;

public class OAIMetadataFormatList {
    private OAIResumptionStream oaiResume;

    public boolean isListValid() {
        return this.oaiResume.isResponseValid();
    }

    protected void frndSetOAIResumptionStream(OAIResumptionStream rs) {
        this.oaiResume = rs;
    }

    public OAIMetadataFormat getCurrentItem() throws OAIException {
        OAIMetadataFormat s = new OAIMetadataFormat();
        Element node = this.oaiResume.getItem();
        if (node != null) {
            try {
                Element node2 = OaiUtil.getXpathNode("oai:metadataPrefix", this.getOAIRepository().getNamespaceNode(), node);
                s.frndSetMetadataPrefix(node2.getValue());
                node2 = OaiUtil.getXpathNode("oai:schema", this.getOAIRepository().getNamespaceNode(), node);
                s.frndSetSchema(node2.getValue());
                node2 = OaiUtil.getXpathNode("oai:metadataNamespace", this.getOAIRepository().getNamespaceNode(), node);
                s.frndSetMetadataNamespace(node2.getValue());
                if (node2 != null) {
                    s.frndSetMetadataNamespace(node2.getValue());
                }
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
}

