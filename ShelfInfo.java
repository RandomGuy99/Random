package com.verizon.delphi.model;

import java.util.List;

public class ShelfInfo {

	private String id;
	private String label;
	private String _class;
	private List<SlotInfo> children;
	private FanInfo fan;
	
	public FanInfo getFan() {
		return fan;
	}
	public void setFan(FanInfo fan) {
		this.fan = fan;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String get_class() {
		return _class;
	}
	public void set_class(String _class) {
		this._class = _class;
	}
	public List<SlotInfo> getChildren() {
		return children;
	}
	public void setChildren(List<SlotInfo> children) {
		this.children = children;
	}
	
}
