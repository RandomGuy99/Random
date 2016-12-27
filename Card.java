package com.verizon.delphi.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Card {
 private String id;
 private List<Port> portList;
 private List<Module> moduleList;
 private Map<String,String> attributeMap;
public Map<String, String> getAttributeMap() {
	return attributeMap;
}
public void setAttributeMap(Map<String, String> attributeMap) {
	this.attributeMap = attributeMap;
}
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

}
