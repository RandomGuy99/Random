import java.io.File;
import java.io.FileOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PatchChords {

	public static void main(String[] args) throws Exception {

		StringBuilder sb = new StringBuilder();
		
		DocumentBuilderFactory factory =
		DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		
		Document doc = builder.parse(new File("C:/MicroServices/NeatTest/Cisco/patchcords.xml"));
		
		Element root = doc.getDocumentElement();
		NodeList pCordList = root.getElementsByTagName("patchcord");
		int n = pCordList.getLength();
		System.out.println("Number of patchcords : "+n);
		for(int i=0;i<n;i++) {
			Node node = pCordList.item(i);
			Node fNode = getNextSibling(node);
			int toPort=0;
			if("from_unit".equals(fNode.getNodeName())) {
				String fromUnit = fNode.getTextContent();
				fNode = getNextSibling(fNode.getNextSibling());
				String toShelf = fNode.getTextContent();
				fNode = getNextSibling(fNode.getNextSibling());
				String toSlot = fNode.getTextContent();
				fNode = getNextSibling(fNode.getNextSibling());
				String frPort = fNode.getTextContent();
				int fromPortTx = (Integer.parseInt(frPort)*2);
				fNode = getNextSibling(fNode.getNextSibling());
				String Port = fNode.getTextContent();
				if((Integer.parseInt(Port))%2==0){
			    toPort = Integer.parseInt(Port);	
				}
				else{
				toPort= Integer.parseInt(Port)+1;
				}
				StringBuilder aidSb = new StringBuilder();
				StringBuilder fromSb = new StringBuilder();
				StringBuilder toSb = new StringBuilder();
								
				fromSb.append(fromUnit).append("-").append(fromPortTx).append("-").append("TX");
				toSb.append(toShelf).append("-").append(toSlot).append("-").append(toPort).append("-").append("RX");
				
				aidSb.append("<AID Name=\"PSLINE-"+fromSb).append("|").append("LINE-").append(toSb).append("\"").append(">").append("\n");
				aidSb.append("\t").append("<KeyWord TOAID=").append("\"LINE-"+toSb).append("|").append("PSLINE-").append(fromSb).append("\"");
				aidSb.append(" CONNRESULT=\"CONNECTED\" ILRESULT=\"OKAY\" EXCESSIL=\"0\">").append("\n").append("\t").append("</KeyWord>").append("\n");
				aidSb.append("</AID>");
				sb.append(aidSb).append("\n");
				System.out.println(aidSb.toString());
			}
			 int fromPort=0;
			 if("from_shelf".equals(fNode.getNodeName())) {
				String fromShelf = fNode.getTextContent();
				fNode = getNextSibling(fNode.getNextSibling());
				String fromSlot = fNode.getTextContent();
				fNode = getNextSibling(fNode.getNextSibling());
				String toUnit = fNode.getTextContent();
				fNode = getNextSibling(fNode.getNextSibling());
				String fPort = fNode.getTextContent(); 
				if((Integer.parseInt(fPort))%2==0){
				 fromPort = Integer.parseInt(fPort);	
				}
				else{
					fromPort = Integer.parseInt(fPort)+1;	
				}
					
				fNode = getNextSibling(fNode.getNextSibling());
				String tPort = fNode.getTextContent(); 
				int toPortRx = ((Integer.parseInt(tPort))*2)-1;
				StringBuilder aidSb = new StringBuilder();
				StringBuilder fromSb = new StringBuilder();
				StringBuilder toSb = new StringBuilder();
				
				fromSb.append(fromShelf).append("-").append(fromSlot).append("-").append(fromPort).append("-").append("TX");
				toSb.append(toUnit).append("-").append(toPortRx).append("-").append("RX");
				
				aidSb.append("<AID Name=\"LINE-"+fromSb).append("|").append("PSLINE-").append(toSb).append("\"").append(">").append("\n");
				aidSb.append("\t").append("<KeyWord TOAID=").append("\"PSLINE-"+toSb).append("|").append("LINE-").append(fromSb).append("\"");
				aidSb.append(" CONNRESULT=\"CONNECTED\" ILRESULT=\"OKAY\" EXCESSIL=\"0\">").append("\n").append("\t").append("</KeyWord>").append("\n");
				aidSb.append("</AID>");
				sb.append(aidSb).append("\n");
				System.out.println(aidSb.toString());
			}
		}
		System.out.println(sb.toString());
		FileOutputStream fos = new FileOutputStream(new File("C:/MicroServices/NeatTest/Cisco/AID.xml"));
		fos.write(sb.toString().getBytes());
	}
	
	public static Node getNextSibling(Node sibling) {
		if("patchcord".equals(sibling.getNodeName())) {
			sibling = sibling.getFirstChild();
		}
		while ((!(sibling instanceof Element) && sibling != null)) {
			sibling = sibling.getNextSibling();
		}
		return sibling;
	}
	
}
