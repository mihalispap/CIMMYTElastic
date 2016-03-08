/*
 * Decompiled with CFR 0_110.
 * 
 * Could not load the following classes:
 *  org.jdom.Attribute
 *  org.jdom.Element
 */
package uiuc.oai;

import java.util.List;
import org.jdom.Attribute;
import org.jdom.Element;
import uiuc.oai.OAIException;
import uiuc.oai.OAIRecord;
import uiuc.oai.OAIRepository;
import uiuc.oai.OAIResumptionStream;

public class OAIRecordList {
    private OAIRecord recCurrent;
    private String strMetadataPrefix;
    private OAIResumptionStream oaiResume;

    public OAIRecord getCurrentItem() {
        return this.recCurrent;
    }

    public int getCurrentIndex() throws OAIException {
        return this.oaiResume.getIndex();
    }

    public int getCompleteSize() throws OAIException {
        return this.oaiResume.getCompleteSize();
    }

    private void priSetCurrentItem() throws OAIException {
        Element node = this.oaiResume.getItem();
        OAIRecord s = new OAIRecord();
        if (node != null) {
            s.frndSetRepository(this.getOAIRepository());
            s.frndSetValid(this.isListValid());
            if (node.getName().equals("identifier")) {
                s.frndSetIdOnly(true);
                s.frndSetIdentifier(node.getText());
                Attribute attr = node.getAttribute("status");
                if (attr != null) {
                    s.frndSetStatus(attr.getValue());
                }
            } else if (node.getName().equals("header")) {
                s.frndSetIdOnly(true);
                s.frndSetMetadataPrefix(this.strMetadataPrefix);
                List nl = node.getChildren();
                for (int i = 0; i < nl.size(); ++i) {
                    if (((Element)nl.get(i)).getName().equals("identifier")) {
                        s.frndSetIdentifier(((Element)nl.get(i)).getText());
                        continue;
                    }
                    if (!((Element)nl.get(i)).getName().equals("datestamp")) continue;
                    s.frndSetDatestamp(((Element)nl.get(i)).getTextTrim());
                }
                Attribute status = node.getAttribute("status");
                if (status != null) {
                    s.frndSetStatus(status.getValue());
                }
                s.frndSetRecord(node);
            } else if (node.getName().equals("record")) {
                int i;
                Element n = null;
                List nlist = node.getChildren();
                for (i = 0; i < nlist.size(); ++i) {
                    if (!((Element)nlist.get(i)).getName().equals("header")) continue;
                    n = (Element)nlist.get(i);
                    break;
                }
                if (n != null) {
                    s.frndSetIdOnly(false);
                    s.frndSetMetadataPrefix(this.strMetadataPrefix);
                    nlist = n.getChildren();
                    for (i = 0; i < nlist.size(); ++i) {
                        if (((Element)nlist.get(i)).getName().equals("identifier") && ((Element)nlist.get(i)).getText() != null) {
                            s.frndSetIdentifier(((Element)nlist.get(i)).getText());
                            continue;
                        }
                        if (!((Element)nlist.get(i)).getName().equals("datestamp") || ((Element)nlist.get(i)).getText() == null) continue;
                        s.frndSetDatestamp(((Element)nlist.get(i)).getText());
                    }
                    Attribute status = n.getAttribute("status");
                    if (status != null) {
                        s.frndSetStatus(status.getValue());
                    }
                    s.frndSetRecord(node);
                } else {
                    s = null;
                }
            } else {
                throw new OAIException(12, "Element " + node.getName() + " is unknown");
            }
            this.recCurrent = s;
        } else {
            this.recCurrent = null;
        }
        //System.out.println("Got here?Q!");
    }

    public boolean isListValid() {
        return this.oaiResume.isResponseValid();
    }

    public boolean moreItems() throws OAIException {
        return this.oaiResume.more();
    }

    public void moveNext() throws OAIException {
        this.oaiResume.moveNext();
        this.priSetCurrentItem();
    }

    public OAIRepository getOAIRepository() {
        return this.oaiResume.getRepository();
    }

    public void requery() throws OAIException {
        this.oaiResume.requery();
        this.priSetCurrentItem();
    }

    protected void frndSetOAIResumptionStream(OAIResumptionStream rs) throws OAIException {
        this.oaiResume = rs;
        this.priSetCurrentItem();
    }

    protected void frndSetMetadataPrefix(String meta) {
        this.strMetadataPrefix = meta;
    }
}

