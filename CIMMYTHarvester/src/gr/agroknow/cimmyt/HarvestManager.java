package gr.agroknow.cimmyt;

import java.io.File;
import java.io.IOException;

import org.ariadne.util.IOUtilsv2;
import org.ariadne.util.OaiUtils;
import org.jdom.Element;
import org.jdom.JDOMException;

import gr.agroknow.cimmyt.utils.DBHandler;
import gr.agroknow.metadata.harvester.Record;
import uiuc.oai.OAIException;
import uiuc.oai.OAIRecord;
import uiuc.oai.OAIRecordList;
import uiuc.oai.OAIRepository;
import uiuc.oai.OAISet;
import uiuc.oai.OAISetList;

public class HarvestManager {

	public static void main(String[] args) throws OAIException, IOException, JDOMException {
        
        
        
        if (args.length != 3) {
            System.err.println("Usage2: java HarvestProcess param1(target) param2(foldername) param3(metadataPrefix), e.g");                
            System.exit(1);
        } 
//        else{ throw new IOException("ERRROR");}    
        
        //"http://ageconsearch.umn.edu/dspace-oai/request" C:\Users\Mihalis\Desktop\testsets\  "oai_dc"
        
       listRecords(args[0],args[1],args[2]);           
              

    //   listRecords("http://jme.collections.natural-europe.eu/oai/","C:/testSet","oai_dc","");
    }

   



	public static void listRecords(String target, String folderName, String metadataPrefix) throws OAIException,IOException, JDOMException 
	{	
		OAIRepository repos = new OAIRepository();
		File file = new File(folderName);
	    String identifier = "";
		file.mkdirs();

		repos.setBaseURL(target);

		OAISetList setList=repos.listSets();		
		System.out.println(setList.toString());
		
		DBHandler db;

		String conf_file=folderName + "/" + "cimmyt.conf";
		db=new DBHandler(conf_file);
		
		int counter = 0;
		while (setList.moreItems()) 
		{
			
			counter++;
			OAISet item = setList.getCurrentItem();
			
			/*
			 * 	TODO: perhaps handle "for CIMMYT staff" differently?
			 * 
			 * 	in an if here?
			 * 
			 * */
			
			String set_name=item.getSetName().toString();
			String set_spec=item.getSetSpec().toString();
			
			CimmytSet current_set=new CimmytSet();
			current_set.setSetName(set_name);
			current_set.setSetSpec(set_spec);
			
			System.out.println(current_set.getSetSpec());
			db.addSet(current_set);
			setList.moveNext();
		}
		
		System.out.println(counter);
		
		db.harvestSets(target, folderName, metadataPrefix);
		
		db.writeToFile(conf_file);
		
	}



}

