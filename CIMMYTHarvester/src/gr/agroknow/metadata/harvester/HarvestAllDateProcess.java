/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.agroknow.metadata.harvester;

import java.io.File;
import java.io.IOException;
import org.ariadne.util.IOUtilsv2;
import org.ariadne.util.OaiUtils;
import org.jdom.Element;
import org.jdom.JDOMException;
import uiuc.oai.OAIException;
import uiuc.oai.OAIRecord;
import uiuc.oai.OAIRecordList;
import uiuc.oai.OAIRepository;

/**
 *
 * @author nimas
 */
public class HarvestAllDateProcess {
    
     public static void main(String[] args) throws OAIException, IOException, JDOMException {
           
           if (args.length != 6) {
                System.err.println("Usage3: java HarvestProcess param1(target) param2(foldername) param3(metadataPrefix) param4(untilD) param5(fromD) param6(Set)");
                System.exit(1);
            }          
           listRecords(args[0],args[1],args[2], args[3], args[4], args[5]);           
        /*
         * 
         * RUNNING CONFs: 
         * "http://unfao.koha-ptfs.eu/cgi-bin/koha/oai.pl" C:\Users\Mihalis\Desktop\XF0\  "oai_dc" 2015-12-31 2014-12-31 XF0
         * 
         * "http://eviikki.hulib.helsinki.fi/agris_harvest/OAI/Server" C:\Users\Mihalis\Desktop\eviikii\  "marc21" 2016-12-31 1970-01-01 Unknown
         * 
         * "http://21bs.ru/index.php/index/oai" C:\Users\Mihalis\Desktop\Biosfera\  "oai_dc" 2016-01-15 2015-09-15 bio
         * 
         * "https://openknowledge.worldbank.org/oai/request" C:\Users\Mihalis\Desktop\worldbank\  "oai_dc" 2013-07-31 1970-01-01 AgroKnow
         * 
         * */
        //   listRecords("http://jme.collections.natural-europe.eu/oai/","C:/testSet","oai_dc","");
        }

       



	public static void listRecords(String target, String folderName, String metadataPrefix, String until, String from, String SetSpec) throws OAIException,IOException, JDOMException {



		OAIRepository repos = new OAIRepository();
		File file = new File(folderName);
                String identifier = "";
		file.mkdirs();

                

              repos.setBaseURL(target);
 
              OAIRecordList records;

		//OAIRecordList records = repos.listRecords("ese","9999-12-31","2000-12-31","");                              
	    
               if (SetSpec=="")
               records = repos.listRecords(metadataPrefix,until,from);              
              else 
               records = repos.listRecords(metadataPrefix,until,from,SetSpec);
               
               int counter = 0;
		//		records.moveNext();
		while (records.moreItems()) {
			counter++;
			OAIRecord item = records.getCurrentItem();

			/*get the lom metadata : item.getMetadata();
			 * this return a Node which contains the lom metadata.
			 */
			if(!item.deleted()) {
				Element metadata = item.getMetadata();
				if(metadata != null) {
					//System.out.println(item.getIdentifier());
					Record rec = new Record();
					rec.setOaiRecord(item);
					rec.setMetadata(item.getMetadata());
					rec.setOaiIdentifier(item.getIdentifier());
                                        identifier = item.getIdentifier().replaceAll(":", "_");
                                        identifier = identifier.replaceAll("/",".");
					IOUtilsv2.writeStringToFileInEncodingUTF8(OaiUtils.parseLom2Xmlstring(metadata), folderName + "/" + identifier +".xml");

				}
				else {
					System.out.println(item.getIdentifier() + " deleted");
				}
			}
                        else {
					System.out.println(item.getIdentifier() + " deleted");
				}
			records.moveNext();
		}
		System.out.println(counter);
	}
    
}
