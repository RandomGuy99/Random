package com.sudha.xml2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PatternTest {

	public static void main(String[] args) {
		Map<String, String> test = new HashMap<String, String>();
		test.put("SLOT-2-1", "SLOT-{SHELFID}-{SLOTID}");
		test.put("SLOT-2-2", "SLOT-{SHELFID}-{SLOTID}");
		test.put("SLOT-1-1", "SLOT-{SHELFID}-{SLOTID}");
		test.put("SLOT-1-2", "SLOT-{SHELFID}-{SLOTID}");
		test.put("PPM-1-1", "CARD-{SHELFID}-{SLOTID}");
		test.put("PPM-1-2", "CARD-{SHELFID}-{SLOTID}");
		test.put("PPM-2-1", "CARD-{SHELFID}-{SLOTID}");
		test.put("PPM-2-2", "CARD-{SHELFID}-{SLOTID}");
		test.put("PPP-1-1-1", "MODULE-{SHELFID}-{SLOTID}-{MODULE}");
		test.put("PPP-1-1-2", "MODULE-{SHELFID}-{SLOTID}-{MODULE}");
		test.put("PPP-1-2-1", "MODULE-{SHELFID}-{SLOTID}-{MODULE}");
		test.put("PPP-1-2-2", "MODULE-{SHELFID}-{SLOTID}-{MODULE}");
		test.put("PPP-2-1-1", "MODULE-{SHELFID}-{SLOTID}-{MODULE}");
		test.put("PPP-2-1-2", "MODULE-{SHELFID}-{SLOTID}-{MODULE}");
		test.put("PPP-2-2-1", "MODULE-{SHELFID}-{SLOTID}-{MODULE}");
		test.put("PPP-2-2-2", "MODULE-{SHELFID}-{SLOTID}-{MODULE}");
		test.put("PPT-1-1-1", "PORT-{SHELFID}-{SLOTID}-{PORT}");
		test.put("PPT-1-1-2", "PORT-{SHELFID}-{SLOTID}-{PORT}");
		test.put("PPT-1-2-1", "PORT-{SHELFID}-{SLOTID}-{PORT}");
		test.put("PPT-1-2-2", "PORT-{SHELFID}-{SLOTID}-{PORT}");
		test.put("PPT-2-1-1", "PORT-{SHELFID}-{SLOTID}-{PORT}");
		test.put("PPT-2-1-2", "PORT-{SHELFID}-{SLOTID}-{PORT}");
		test.put("PPT-2-2-1", "PORT-{SHELFID}-{SLOTID}-{PORT}");
		test.put("PPT-2-2-2", "PORT-{SHELFID}-{SLOTID}-{PORT}");
		
	Network net =	getNetworkDetails(test);
System.out.println("net details");
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
							for(int i=0;i<shelflength;i++){
								Shelf sh= net.getShelfList().get(i);
								String hid = sh.getId();
								if(hid.equals(a[1]) && sh.getSlotList().size()>0){
								int slotlength = sh.getSlotList().size();
								for(int j=0;j<slotlength;j++){	
								Slot sl= sh.getSlotList().get(j);
								String sid = sl.getId();
								if(sid.equals(a[2]) && sl.getCardList().size() >0 ){
								int cardlength = sl.getCardList().size();
								for(int k=0;k<cardlength;k++){
								Card ca= sl.getCardList().get(k);
									String cid = ca.getId();
									if(cid.equals(a[1]+"-"+a[2])){
										ca.getPortList().add(port);
									}else{
										sl.getCardList().add(card);
									}
								}
								}else{
									sh.getSlotList().add(slot);
								}
								}
								}else{
								
								net.getShelfList().add(shelf);
								
								}
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
							for(int i=0;i<shelflength;i++){
								Shelf sh= net.getShelfList().get(i);
								String hid = sh.getId();
								if(hid.equals(a[1]) && sh.getSlotList().size()>0){
									int slotlength = sh.getSlotList().size();
									for(int j=0;j<slotlength;j++){	
									Slot sl= sh.getSlotList().get(j);
								String sid = sl.getId();
								if(sid.equals(a[2]) && sl.getCardList().size() >0 ){
									int cardlength = sl.getCardList().size();
									for(int k=0;k<cardlength;k++){
									Card ca= sl.getCardList().get(k);
									String cid = ca.getId();
									if(cid.equals(a[1]+"-"+a[2])){
										ca.getModuleList().add(module);
									}else{
										sl.getCardList().add(card);
									}
								}
								}else{
									sh.getSlotList().add(slot);
								}
								}
								}else{
								
								net.getShelfList().add(shelf);
								
								}
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
					for(int i=0;i<shelflength;i++){
						Shelf sh= net.getShelfList().get(i);
						String hid = sh.getId();
						if(hid.equals(a[1]) && sh.getSlotList().size()>0){
							int slotlength = sh.getSlotList().size();
							for(int j=0;j<slotlength;j++){	
							Slot sl= sh.getSlotList().get(j);
						String sid = sl.getId();
						if(sid.equals(a[2]) && sl.getCardList().size() >0 ){
						sl.getCardList().add(card);
						}else{
							sh.getSlotList().add(slot);
						}
						}
						}else{
						net.getShelfList().add(shelf);
						}
					}
				}
			}
 
            if(null!=form&&form.length==2){
            	Shelf shelf = new Shelf();
				shelf.setId(a[1]);
				Slot slot = new Slot();
				slot.setId(a[2]);
				shelf.getSlotList().add(slot);
				if(net.getShelfList().size()==0){
				net.getShelfList().add(shelf);
				}else{
					int shelflength = net.getShelfList().size();
					for(int i=0;i<shelflength;i++){
						Shelf sh= net.getShelfList().get(i);
						String hid = sh.getId();
						if(hid.equals(a[1]) && sh.getSlotList().size()>0){
						sh.getSlotList().add(slot);
						}else{
						net.getShelfList().add(shelf);
						}
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

