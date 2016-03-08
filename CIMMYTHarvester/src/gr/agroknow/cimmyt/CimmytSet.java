package gr.agroknow.cimmyt;

import uiuc.oai.OAISet;

public class CimmytSet extends OAISet {

	private String last_indexed;
	
	public void setSetName(String name){
		this.frndSetSetName(name);
	}

	public void setSetSpec(String spec){
		this.frndSetSetSpec(spec);
	}

	public void setLastIndexed(String li)
	{
		this.last_indexed=li;
	}
	
	public String getLastIndexed()
	{
		return this.last_indexed;
	}


	public String toFile()
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
