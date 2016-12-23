package com.verizon.delphi.model;

import java.util.ArrayList;
import java.util.List;

public class Shelf {
	private String id;
	private Fan fan;
	public Fan getFan() {
		return fan;
	}
	public void setFan(Fan fan) {
		this.fan = fan;
	}
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
