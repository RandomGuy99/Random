import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Equip {

	public static void main(String[] args) {
		Map<String, String> test = new HashMap<String, String>();

		test.put("PSUSB-51-14","CARD-{SHELF}-{SLOT}");
		test.put("FAN-2-1"," FAN-{SHELF}-{SLOT}");
		test.put("FAN-1-1","FAN-{SHELF}-{SLOT}");
		test.put("SLOT-1-17 "," CARD-{SHELF}-{SLOT}");
		test.put("PWRM-1-2 "," MODULE-{SHELF}-{SLOT}");
		test.put("PWRM-2-1 "," MODULE-{SHELF}-{SLOT}");
		test.put("SLOT-1-12 "," CARD-{SHELF}-{SLOT}");
		test.put("PPM-1-17-1 "," MODULE-{SHELF}-{SLOT}-{MODULE}");
		test.put("PWRM-1-1 "," MODULE-{SHELF}-{SLOT}");
		test.put("SLOT-1-8 "," CARD-{SHELF}-{SLOT}");
		test.put("SLOT-1-6 "," CARD-{SHELF}-{SLOT}");
		test.put("PWRM-2-4 "," MODULE-{SHELF}-{SLOT}");
		test.put("PWRM-1-4 "," MODULE-{SHELF}-{SLOT}");
		test.put("PWRM-2-3 "," MODULE-{SHELF}-{SLOT}");
		test.put("ECU-1-18 "," ECU-{SHELF}-{SLOT}");
		test.put("SLOT-1-10 "," CARD-{SHELF}-{SLOT}");
		test.put("PWRM-1-3 "," MODULE-{SHELF}-{SLOT}");
		test.put("PWRM-2-2 "," MODULE-{SHELF}-{SLOT}");
		test.put("SLOT-1-1 "," CARD-{SHELF}-{SLOT}");
		test.put("SLOT-2-1 ",": CARD-{SHELF}-{SLOT}");
		test.put("USBPORT-1-18-13 "," PORT-{SHELF}-{SLOT}-{PORT}");
		test.put("USBPORT-1-18-14 "," PORT-{SHELF}-{SLOT}-{PORT}");
		
		test.put("ECU-2-18","ECU-{SHELF}-{SLOT}");
		test.put("PPM-1-1-1","MODULE-{SHELF}-{SLOT}-{MODULE}");
		test.put("PSUSB-81-4","CARD-{SHELF}-{SLOT}");
		test.put("PSUSB-81-5","CARD-{SHELF}-{SLOT}");
		test.put("SLOT-2-17","CARD-{SHELF}-{SLOT}"); 
		test.put("PSUSB-81-6","CARD-{SHELF}-{SLOT},");
		test.put("PSUSB-81-7","CARD-{SHELF}-{SLOT}");
		test.put("PSUSB-51-1","CARD-{SHELF}-{SLOT}");
		
	Network net =	getNetworkDetails(test);
System.out.println("net details"+net);
	}
	
	public static Network getNetworkDetails(Map<String, String> test){
		Network net = new Network();
		for (Map.Entry<String, String> entry : test.entrySet()) {
			String aid = entry.getKey();
			String format = entry.getValue();
			String[] form = format.split("-");
			String[] a = aid.split("-");
			System.out.println("Key : " + aid + " Value : " +format );
            if(null!=form&&form.length==4){
            	Shelf shelf = new Shelf();
				shelf.setId(a[1]);
				Slot slot = new Slot();
				slot.setId(a[2]);
				Card card = new Card();
				card.setId(a[1]+"-"+a[2]);
					if("PORT".equals(form[0])){
						Port port = new Port();
						port.setId(a[3]);
						card.getPortList().add(port);
						slot.getCardList().add(card);
						shelf.getSlotList().add(slot);
						if(net.getShelfList().size()==0){
						net.getShelfList().add(shelf);
						}else{
							int shelflength = net.getShelfList().size();
							boolean isShelf = true;
							for(int i=0;i<shelflength;i++){
								Shelf sh= net.getShelfList().get(i);
								String hid = sh.getId();
								if(hid.equals(a[1])){
								int slotlength = sh.getSlotList().size();
								boolean isSlot = true;
								for(int j=0;j<slotlength;j++){	
								Slot sl= sh.getSlotList().get(j);
								String sid = sl.getId();
								if(sid.equals(a[2]) ){
								int cardlength = sl.getCardList().size();
								boolean isCard = true;
								for(int k=0;k<cardlength;k++){
								Card ca= sl.getCardList().get(k);
									String cid = ca.getId();
									if(cid.equals(a[1]+"-"+a[2])){
										ca.getPortList().add(port);
										isCard = false;
										isSlot=false;
										isShelf=false;
									}}						
								    if(isCard){
									    isSlot=false;
									    isShelf=false;
										sl.getCardList().add(card);
									}}}
								if(isSlot){
									isShelf=false;
									sh.getSlotList().add(slot);
								}}}
    						   if(isShelf){
								net.getShelfList().add(shelf);
								}
						}
					}
                    if("MODULE".equals(form[0])){
                    	Module module = new Module();
                    	module.setId(a[3]);
						card.getModuleList().add(module);
						slot.getCardList().add(card);
						shelf.getSlotList().add(slot);
						
						if(net.getShelfList().size()==0){
						net.getShelfList().add(shelf);
						}else{
							int shelflength = net.getShelfList().size();
							boolean isShelf = true;
							for(int i=0;i<shelflength;i++){
								Shelf sh= net.getShelfList().get(i);
								String hid = sh.getId();
								if(hid.equals(a[1])){
									int slotlength = sh.getSlotList().size();
									boolean isSlot = true;
									for(int j=0;j<slotlength;j++){	
									Slot sl= sh.getSlotList().get(j);
								String sid = sl.getId();
								if(sid.equals(a[2]) ){
									int cardlength = sl.getCardList().size();
									boolean isCard = true;
									for(int k=0;k<cardlength;k++){
									Card ca= sl.getCardList().get(k);
									String cid = ca.getId();
									if(cid.equals(a[1]+"-"+a[2])){
										ca.getModuleList().add(module);
										isCard = false;
										isSlot=false;
										isShelf=false;
									}}						
									if(isCard){
										isSlot=false;
										isShelf=false;
										sl.getCardList().add(card);
									}}}
								if(isSlot){
									isShelf=false;
									sh.getSlotList().add(slot);
								}}}
        						if(isShelf){
								net.getShelfList().add(shelf);
								}
							
						}
					}
			}
            if(null!=form&&form.length==3&&"CARD".equals(form[0])){
            	Shelf shelf = new Shelf();
				shelf.setId(a[1]);
				Slot slot = new Slot();
				slot.setId(a[2]);
				Card card = new Card();
				card.setId(a[1]+"-"+a[2]);
				slot.getCardList().add(card);
				shelf.getSlotList().add(slot);
				if(net.getShelfList().size()==0){
				net.getShelfList().add(shelf);
				}else{
					int shelflength = net.getShelfList().size();
					boolean isShelf = true;
					for(int i=0;i<shelflength;i++){
						Shelf sh= net.getShelfList().get(i);
						String hid = sh.getId();
						if(hid.equals(a[1])){
							int slotlength = sh.getSlotList().size();
							boolean isSlot = true;
							for(int j=0;j<slotlength;j++){	
							Slot sl= sh.getSlotList().get(j);
						String sid = sl.getId();
						if(sid.equals(a[2])){
							if(!(hid+"-"+sid).equals(card.getId())){
						   sl.getCardList().add(card);
							}
						   isSlot=false;
						   isShelf = false;
						}}
						if(isSlot){
								isShelf=false;
							sh.getSlotList().add(slot);
						}
						}
						}
					   if(isShelf){
						net.getShelfList().add(shelf);
						}
				}
			}
 
            if(null!=form&&form.length==3&&"SLOT".equals(form[0])){
            	Shelf shelf = new Shelf();
				shelf.setId(a[1]);
				Slot slot = new Slot();
				slot.setId(a[2]);
				shelf.getSlotList().add(slot);
				if(net.getShelfList().size()==0){
				net.getShelfList().add(shelf);
				}else{
					int shelflength = net.getShelfList().size();
					boolean isShelf = true;
					for(int i=0;i<shelflength;i++){
						Shelf sh= net.getShelfList().get(i);
						String hid = sh.getId();
						if(hid.equals(a[1])){
							if(!(a[2]).equals(slot.getId())){	
						sh.getSlotList().add(slot);
							}
						isShelf = false;
						}}
					    if(isShelf){
						net.getShelfList().add(shelf);
						}
				}
	       }
			
		}	
		return net;
	}
}	
	
	class Network{
		private List<Shelf> shelfList;

		public List<Shelf> getShelfList() {
			if(shelfList==null){
				shelfList = new ArrayList<Shelf>();
			}
			return shelfList;
		}
		
	}
	
class Shelf{
	private String id;
	private List<Slot> slotList;
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<Slot> getSlotList() {
		if(slotList==null){
			slotList = new ArrayList<Slot>();
		}
		return slotList;
	}	
	
	
	}

class Slot{
	private String id;
	private List<Card> cardList;
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<Card> getCardList() {
		if(cardList==null){
			cardList = new ArrayList<Card>();
		}
		return cardList;
	}	
	
}

class Card{
	private String id;
    private List<Port> portList;
    private List<Module> moduleList;
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<Port> getPortList() {
		if(portList==null){
			portList = new ArrayList<Port>();
		}
		return portList;
	}

	public List<Module> getModuleList() {
		if(moduleList==null){
			moduleList = new ArrayList<Module>();
		}
		return moduleList;
	}

	public void setModuleList(List<Module> moduleList) {
		this.moduleList = moduleList;
	}
	
}

class Port{
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
}

class Module{
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}