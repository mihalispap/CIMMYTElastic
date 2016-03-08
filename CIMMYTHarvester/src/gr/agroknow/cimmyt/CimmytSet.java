package gr.agroknow.cimmyt;

import uiuc.oai.OAISet;

public class CimmytSet extends OAISet {

	void setSetName(String name){
		this.frndSetSetName(name);
	}

	void setSetSpec(String spec){
		this.frndSetSetSpec(spec);
	}

	String toFile()
	{
		String fileOutput="";
		
		/*Sample
		 * 
		 * 
		 * 	<set>
				<setSpec>
					hdl_162105
				</setSpec>
				<setName>
					100th Seminar, June 21-23, 2007, Novi Sad, Serbia and Montenegro
				</setName>
			</set>
		 * 
		 * */
		
		fileOutput="<set>\n\t<setSpec>\n\t\t"+this.getSetSpec()+"\n\t</setSpec>\n\t<setName>\n\t\t"+
				this.getSetName()+"\n\t</setName>\n</set>";
		
		return fileOutput;
	}
	
}
