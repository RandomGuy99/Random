package com.verizon.delphi.service.impl;


import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.codehaus.jackson.map.ObjectMapper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.stereotype.Service;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.verizon.delphi.config.BackEndWrapper;
import com.verizon.delphi.config.BackEndWrapper.SvcName;
import com.verizon.delphi.config.RedisWrapper;
import com.verizon.delphi.dao.FlowDao;
import com.verizon.delphi.dms.config.CommonService;
import com.verizon.delphi.model.AidsInfo;
import com.verizon.delphi.model.Attributes;
import com.verizon.delphi.model.Card;
import com.verizon.delphi.model.EquipNodeInfo;
import com.verizon.delphi.model.EquipmentView;
import com.verizon.delphi.model.Fan;
import com.verizon.delphi.model.FanInfo;
import com.verizon.delphi.model.Module;
import com.verizon.delphi.model.NeatInfo;
import com.verizon.delphi.model.NeatSourceInfo;
import com.verizon.delphi.model.NetAssistInfo;
import com.verizon.delphi.model.NetworkElements;
import com.verizon.delphi.model.NodeAttribute;
import com.verizon.delphi.model.NodeInfo;
import com.verizon.delphi.model.Port;
import com.verizon.delphi.model.CardInfo;
import com.verizon.delphi.model.PortModuleInfo;
import com.verizon.delphi.model.Shelf;
import com.verizon.delphi.model.ShelfInfo;
import com.verizon.delphi.model.SignalFlow;
import com.verizon.delphi.model.Slot;
import com.verizon.delphi.model.SlotInfo;
import com.verizon.delphi.model.Topology;
import com.verizon.delphi.model.TopologyResponse;
import com.verizon.delphi.service.FlowService;
import com.verizon.delphi.utils.FlowServiceConstants;


@Service("FlowService")
public class FlowServiceImpl  implements FlowService {
    
	private static final Logger logger = LoggerFactory
			.getLogger(FlowServiceImpl.class);
     
	
	public HashOperations<String, Object, Object> hashOps;
    @Autowired
    public FlowDao flowDao;
    @Autowired
    public RedisWrapper redisWrapper;
	@Autowired
	BackEndWrapper backEndWrapper;
	@Autowired
	CommonService commonService; 
 	public JSONObject getTopology(String input)throws Exception {

		NeatInfo neatInfo = null;
		TopologyResponse topologyResponse = null;
		JSONParser jsonParser = new JSONParser();
		JSONObject jsonObject = null;
		JSONObject jsonObj=null;
		//boolean tidflag=true;
		boolean netassistflag=false;
		boolean modelflag=false;
		boolean templateFlag=false;
		String degree=null;
		String model=null;
		try {
			jsonObject = (JSONObject) jsonParser.parse(input);  
			String tid = (String) jsonObject.get("id");
			model = (String) jsonObject.get("model");
		    degree = (String) jsonObject.get("degree");
			logger.info("tid:"+tid);
			logger.info("model:"+model);
			logger.info("degree:"+degree);
			 if(null!=model && !(model.isEmpty()))
			 model=commonService.getModelAlias(model);
			 logger.info("model after replace with alias:"+model);
			JSONObject netassistresponse=null;
			

			if (null==model || model.isEmpty())
			{
				
				try
				{
					netassistresponse = getNetAssistResponse(tid);
				netassistflag=true;
				modelflag=false;
				}
				catch(Exception e)
				{
					netassistflag=false;
					modelflag=false;
					logger.error(" Error while getting NetAssist service response model empty " + e.getMessage());
					e.printStackTrace();
					String errorMsg=e.getMessage();
					throw new Exception("NetAssist Service is down");
				}
			}
			else if(!(model.isEmpty()))
			{
				try
				{
				modelflag=true;
				netassistresponse = getNetAssistResponse(tid);
				netassistflag=true;
			
				}
				catch(Exception e)
				{
					netassistflag=false;
					logger.error(" Error while getting NetAssist service response " + e.getMessage());
					e.printStackTrace();
					String errorMsg=e.getMessage();
					//throw new Exception("NetAssist Service is down");
				}
			}
			
			//if netassist flag true get the  model from netassit response and send the model to db to fetch source xml data
			logger.info("netassistflag::"+netassistflag);
			if(netassistflag)
			{
				 model=JsonPath.read(netassistresponse, "$.TID_DETAILS.NETWORK_ELEMENT_DATA.equipTypeId.typeName");
				 logger.info("model from NetAssist response"+model);
				 if(null!=model && !(model.isEmpty()))
				 model=commonService.getModelAlias(model);				 	 
				 logger.info("model after replace with alias"+model);
				 
			}
			String neatXml = flowDao.getNeatXML(degree,model);			
			if(!(neatXml.isEmpty()))
			{
				templateFlag=true;
				//tidflag=true;
			neatInfo = parseNeatXml(neatXml,degree,model,tid,netassistresponse);
			}	
			topologyResponse = populateTopologyData(neatInfo);
			NetAssistInfo netAssistInfo=new NetAssistInfo();
			netAssistInfo.setTopologyResponse(topologyResponse);
			netAssistInfo.setModel(model);
			netAssistInfo.setDegree(degree);
			netAssistInfo.setTid(tid);
			netAssistInfo.setNetAssistResponse(netassistresponse);
			//setTopologyInCache(tid, topologyResponse);
			setTopologyInCache(netAssistInfo);
			jsonObj = createTopologyResponse(topologyResponse);
		} 
		catch(PathNotFoundException ex){
			throw new Exception("Error while getting  topology");	
		}
		catch (Exception e) {
			logger.error(" Error while getting  topology " + e);
			if(!(netassistflag) && !(modelflag)){
			throw new Exception("NetAssist Service is down");
			}
			if(!templateFlag){ 
				  throw new Exception("Topology not found for "+model+" "+degree);
				}
			else
			{
				throw new Exception("Error while getting  topology");
			}
			
		}

		return jsonObj;
	}
     
	/**
	 * @param fileName
	 * @return neatInfo
	 */
	private NeatInfo parseNeatXml(String fileName,String degree,String model,String tid,JSONObject netassistresponse)throws Exception {
		logger.info("parseNeatXml :: Start");
		NeatInfo neatInfo = new NeatInfo();
		XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
		List<AidsInfo> aidList = new ArrayList<AidsInfo>();
		List<String> conditions = new ArrayList<String>();
	//	List<Map<String,String>> formatList = new ArrayList<Map<String,String>>();
		Map<String,String> formatMap = new HashMap<String,String>();
		Map<String,String> attributeMap = new HashMap<String,String>();
		List<String> commandList = new ArrayList<String>();
		List<NodeAttribute> nodeAttributeList = new ArrayList<NodeAttribute>();
		//try {
			NodeAttribute nodeAttribute = null;
			/* 
			 * XMLEventReader xmlEventReader = xmlInputFactory
			 * .createXMLEventReader(new FileInputStream(fileName));
			 */

			byte[] byteArray = fileName
					.getBytes(FlowServiceConstants.XML_ENCODING);
			ByteArrayInputStream inputStream = new ByteArrayInputStream(
					byteArray);

			XMLEventReader xmlEventReader = xmlInputFactory
					.createXMLEventReader(inputStream);
			while (xmlEventReader.hasNext()) {
				XMLEvent xmlEvent = xmlEventReader.nextEvent();
				if (xmlEvent.isStartElement()) {
					StartElement startElement = xmlEvent.asStartElement();

					if (startElement.getName().getLocalPart()
							.equals(FlowServiceConstants.AID_TAG)) {
						Attribute aidName = startElement
								.getAttributeByName(new QName(
										FlowServiceConstants.NAME_ATTR));
						Attribute format = startElement
								.getAttributeByName(new QName(
										FlowServiceConstants.FORMAT_ATTR));
												
						if (null != aidName && "" != aidName.getValue()) {
							AidsInfo aidInfo = new AidsInfo();
							aidInfo.setAid(aidName.getValue());
							aidInfo.setRateCode("");
							if((null != format && "" != format.getValue())){					
							
							formatMap.put(aidName.getValue(), format.getValue());
						//	formatList.add(formatMap);
							}
							aidList.add(aidInfo);
						}
						if (startElement.getName().getLocalPart()
								.equals(FlowServiceConstants.KeyWord_TAG)) {
							
							Attribute pn = startElement
									.getAttributeByName(new QName(
											FlowServiceConstants.PN));
							if(pn!=null&&format!=null){
								attributeMap.put("PN", pn.getValue());	
							}
						}
												
					} else if (startElement.getName().getLocalPart()
							.equals(FlowServiceConstants.KeyWord_TAG)) {
						/*attributeMap.put("AIDTYPE", aidtype.getValue());*/
					//	attributeMap.put("HWREV", hwRev.getValue());
					//	attributeMap.put("FWREV", fwRev.getValue());
						
						/*Attribute model = startElement
								.getAttributeByName(new QName(
										FlowServiceConstants.MODEL_ATTR));
						if (null != model && "" != model) {
							modelId=model.getValue();*/
							neatInfo.setModel(model);
							nodeAttribute.setModel(model);
						//}

					}
					else if (startElement.getName().getLocalPart()
							.equals(FlowServiceConstants.NODE_TAG)) {
				  	  nodeAttribute = new NodeAttribute();
						aidList = new ArrayList<AidsInfo>();
						/*Attribute tid = startElement
								.getAttributeByName(new QName(
										FlowServiceConstants.NAME_ATTR));
						tidVal=tid.getValue();*/
						
							nodeAttribute.setTid(tid);
							neatInfo.setTid(tid);
						
					}

				} else if (xmlEvent.isEndElement()) {
					EndElement endElement = xmlEvent.asEndElement();

					if (endElement.getName().getLocalPart()
							.equals(FlowServiceConstants.NODE_TAG)) {

						Set<AidsInfo> s = new HashSet<AidsInfo>();
						s.addAll(aidList);
						List<AidsInfo> aidsList = new ArrayList<AidsInfo>();
						aidsList.addAll(s);
						nodeAttribute.setAidlist(aidsList);

						nodeAttributeList.add(nodeAttribute);
					}
				}
			}
			//NeatSourceInfo neatsourceinfo=flowDao.getAvaialableCmds(tidVal);
			NeatSourceInfo neatsourceinfo=flowDao.getAvaialableCmds(degree,model);
			neatInfo.setNetworkList(getInventoryDetails(formatMap));
			neatInfo.setNodeAttribute(nodeAttributeList);
			neatInfo.setNodeclass(FlowServiceConstants.NODE_CLASS);
			neatInfo.setOwnership(neatsourceinfo.getOwnership());
			commandList.add(FlowServiceConstants.AVAIALBLE_CMDS);
			neatInfo.setAvailableCmds(neatsourceinfo.getAvailableCmds());
			neatInfo.setRole(neatsourceinfo.getRole());
			neatInfo.setConditions(conditions);
			neatInfo.setDegree(degree);
			neatInfo.setNetassistresponse(netassistresponse);
			logger.info("parseNeatXml :: End");
		/*} catch (XMLStreamException | UnsupportedEncodingException e) {
			logger.error(" error while parsing the NeatXml" + e);
		}*/
		return neatInfo;

	}

	
	/**
	 * this method populates all the data into toplogy object
	 * @param neatInfo
	 * @return topologyResponse
	 */
	private TopologyResponse populateTopologyData(NeatInfo neatInfo) {

		TopologyResponse topologyResponse = new TopologyResponse();
		Topology topology = new Topology();
		SignalFlow signalFlow = new SignalFlow();
		Attributes attributes = new Attributes();

		PortModuleInfo portModuleInfo = null;		
		CardInfo CardInfo = new CardInfo();		
		SlotInfo slotInfo = new SlotInfo();
		FanInfo fanInfo = null;
		ShelfInfo shelfInfo = new ShelfInfo();
		EquipNodeInfo equipNodeInfo = new EquipNodeInfo(); 
		EquipmentView equipView = new EquipmentView();
		
		List<PortModuleInfo> portInfoList = new ArrayList<PortModuleInfo>();
		List<CardInfo> slotInfoList = new ArrayList<CardInfo>();
		List<SlotInfo> shelfInfoList = new ArrayList<SlotInfo>();
		List<ShelfInfo> equipNodeInfoList = new ArrayList<ShelfInfo>();
		List<EquipNodeInfo> equipviewInfoList = new ArrayList<EquipNodeInfo>();
		
		List<NodeInfo> nodeInfoList = new ArrayList<NodeInfo>();
		List<NodeAttribute> nodeAttributeList = neatInfo.getNodeAttribute();
		List<NodeAttribute> nodeAttrList = new ArrayList<NodeAttribute>();
		NodeInfo children = null;

		NodeAttribute nodeAttribute = null;
		String nodeToken = "";
		String equipToken = "";
		String topologyToken = "";
		topologyToken = createTopologyToken();
		List commandList=new ArrayList();
		for (NodeAttribute nodeAttr : nodeAttributeList) {
			nodeAttribute = new NodeAttribute();
			nodeToken = topologyToken + "-" + createTopologyToken();
			children = new NodeInfo();
			children.set_class(neatInfo.getNodeclass());
			children.setId(nodeToken);
			children.setLabel(nodeAttr.getTid()+" ("+neatInfo.getRole()+")");
			nodeInfoList.add(children);
			nodeAttribute.setOwnership(neatInfo.getOwnership());
			nodeAttribute.setAvailableCmds(neatInfo.getAvailableCmds());
			nodeAttribute.setTid(nodeAttr.getTid());
			nodeAttribute.setRole(neatInfo.getRole());
			nodeAttribute.setConditions(neatInfo.getConditions());
			nodeAttribute.setModel(nodeAttr.getModel());
			nodeAttribute.setDegree(neatInfo.getDegree());
			/*if(nodeAttribute.getType()==null||nodeAttribute.getType()==""){
			nodeAttribute.setType("");
			}
			else{
		    nodeAttribute.setType(neatInfo.getType());
			}*/
			nodeAttribute.setAidlist(nodeAttr.getAidlist());
			nodeAttribute.setNodeId(nodeToken);
			nodeAttrList.add(nodeAttribute);
		}
		signalFlow.setId("");
		signalFlow.setLabel(neatInfo.getTid());
		signalFlow.setChildren(nodeInfoList);
		attributes.setNodeAttribute(nodeAttrList);
		for(Shelf sh : neatInfo.getNetworkList().getShelfList()){
			shelfInfoList = new ArrayList<SlotInfo>();
			for(Slot st :sh.getSlotList()){
				slotInfoList = new ArrayList<CardInfo>();
				for(Card ca : st.getCardList()){
					portInfoList = new ArrayList<PortModuleInfo>();
					for(Port pt : ca.getPortList()){
						portModuleInfo = new PortModuleInfo();
						portModuleInfo.setId("EV-"+sh.getId()+"|"+st.getId()+"|"+ca.getId()+"|"+pt.getId());
						portModuleInfo.setLabel(FlowServiceConstants.PORT_CLASS+"("+pt.getId()+")");
						portModuleInfo.set_class(FlowServiceConstants.PORT_CLASS.toUpperCase());		
						portInfoList.add(portModuleInfo);	
					}
					for(Module md : ca.getModuleList()){
						portModuleInfo = new PortModuleInfo();		
						portModuleInfo.setId("EV-"+sh.getId()+"|"+st.getId()+"|"+ca.getId()+"|"+md.getId());
						portModuleInfo.setLabel(FlowServiceConstants.MODULE_CLASS+"("+md.getId()+")");
						portModuleInfo.set_class(FlowServiceConstants.MODULE_CLASS.toUpperCase());
						portInfoList.add(portModuleInfo);		
					}
					CardInfo = new CardInfo();
					CardInfo.setId("EV-"+sh.getId()+"|"+st.getId());
					CardInfo.setLabel(FlowServiceConstants.CARD_CLASS+"()");
					CardInfo.set_class(FlowServiceConstants.CARD_CLASS.toUpperCase());		
					CardInfo.setChildren(portInfoList);
					slotInfoList.add(CardInfo);
					
				}
				slotInfo = new SlotInfo();
				slotInfo.setId("EV-"+sh.getId()+"|"+st.getId());
				slotInfo.setLabel(FlowServiceConstants.SLOT_CLASS+"("+st.getId()+")");
				slotInfo.set_class(FlowServiceConstants.SLOT_CLASS.toUpperCase());
				slotInfo.setChildren(slotInfoList);
				shelfInfoList.add(slotInfo);
			}
				if(!(sh.getFan()==null)){
					fanInfo= new FanInfo();
					fanInfo.setId("EV-"+sh.getId());
					fanInfo.setLabel(FlowServiceConstants.FAN_CLASS+"()");
					fanInfo.set_class(FlowServiceConstants.FAN_CLASS.toUpperCase());
					
				}
			
			shelfInfo = new ShelfInfo();
			shelfInfo.setId("EV-"+sh.getId());
			shelfInfo.setLabel(FlowServiceConstants.SHELF_CLASS+"("+sh.getId()+")");
			shelfInfo.set_class(FlowServiceConstants.SHELF_CLASS.toUpperCase());
			if(!(sh.getFan()==null)){
				shelfInfo.setFan(fanInfo);
			}
	        shelfInfo.setChildren(shelfInfoList);
	        equipNodeInfoList.add(shelfInfo);
		}
		
        equipToken = topologyToken + "-" + createTopologyToken();
        equipNodeInfo.setId(equipToken);
        equipNodeInfo.setLabel(neatInfo.getTid()+" ("+neatInfo.getRole()+")");
        equipNodeInfo.set_class(FlowServiceConstants.NODE_CLASS);
        equipNodeInfo.setChildren(equipNodeInfoList);
        equipviewInfoList.add(equipNodeInfo);
          
        equipView.setId(topologyToken);
        equipView.setLabel(neatInfo.getTid());
        equipView.setChildren(equipviewInfoList);

		topology.setAttributes(attributes);
		topology.setSignalFlow(signalFlow);
		topology.setEquipmentView(equipView);
		topologyResponse.setTopology(topology);

		return topologyResponse;
	}
	
	/**
	 * this method creates unique identifier  for each topology object
     * @return unique token 
     */
    private String createTopologyToken()
    {
    	logger.info("createTopologyToken :: Start");
    	UUID identifier = UUID.randomUUID();
		String topologyToken="SF"+identifier;  	
		logger.info("createTopologyToken :: End"+topologyToken);
    	return topologyToken;
    }
    
	/**
	 * this method saves the topology object in cache
	 * @param tid
	 * @param topologyResponse
	 */
	/*public void setTopologyInCache(String tid,TopologyResponse topologyResponse)throws Exception {
		String token = topologyResponse.getTopology().getSignalFlow().getId();
		logger.info("setTopologyInCache :: Start"+token);
		logger.info("setTopologyInCache :: tid"+tid);
		try {
			redisWrapper.saveToRedisCache("topology", token, topologyResponse);
			
			ObjectMapper mapper = new ObjectMapper();
			//mapper.writeValueAsString(topologyResponse);
			System.out.println("json topology object---"+mapper.writeValueAsString(topologyResponse));
			logger.info("setTopologyInCache :: End"+token);
		}catch (Exception e) {

			logger.error(" Error while caching topolgy " + e);

		}

    }*/
	public void setTopologyInCache(NetAssistInfo netAssistInfo)throws Exception {
		String token = netAssistInfo.getTopologyResponse().getTopology().getSignalFlow().getId();
		logger.info("setTopologyInCache :: Start"+token);
		try {
			redisWrapper.saveToRedisCache("topology", token, netAssistInfo);
			
			ObjectMapper mapper = new ObjectMapper();
			//mapper.writeValueAsString(topologyResponse);
			logger.info("json topology object---"+mapper.writeValueAsString(netAssistInfo));
			logger.info("setTopologyInCache :: End"+token);
		}catch (Exception e) {

			logger.error(" Error while caching topolgy " + e);

		}

    }

	
	/**
	 * this method converts  the topology object into json object and returns the flow service response in json
	 * @param topologyResponse
	 * @return  jsonObj
	 */
	@SuppressWarnings("unchecked")
	private JSONObject createTopologyResponse(TopologyResponse topologyResponse)throws Exception {

		logger.info("createTopologyResponse :: Start");
		ObjectMapper mapper = null;
		JSONObject jsonObj = null;
		//try {

			mapper = new ObjectMapper();
			logger.info("Topology Object : " + topologyResponse);

			JSONParser jsonParser = new JSONParser();
			jsonObj = (JSONObject) jsonParser.parse(mapper
					.writeValueAsString(topologyResponse));

			JSONObject jsonTopologyObj = (JSONObject) jsonObj
					.get(FlowServiceConstants.TOPOLOGY);
			jsonTopologyObj.put(FlowServiceConstants.SIGNAL_FLOW_DIAGRAM,
					jsonTopologyObj.get(FlowServiceConstants.SIGNAL_FLOW));
			jsonTopologyObj.remove(FlowServiceConstants.SIGNAL_FLOW);
			
			JSONObject jsonsignalObj = (JSONObject) jsonTopologyObj
					.get(FlowServiceConstants.SIGNAL_FLOW_DIAGRAM);
			
			jsonTopologyObj.put(FlowServiceConstants.EQUIMENT_VIEW_DIAGRAM,
					jsonTopologyObj.get(FlowServiceConstants.EQUIMENT_VIEW));
			jsonTopologyObj.remove(FlowServiceConstants.EQUIMENT_VIEW);
			
			JSONObject jsonequipObj = (JSONObject) jsonTopologyObj
					.get(FlowServiceConstants.EQUIMENT_VIEW_DIAGRAM);

			JSONArray jsonsigArray = (JSONArray) jsonsignalObj
					.get(FlowServiceConstants.CHILDREN);
			for (int i = 0; i < jsonsigArray.size(); i++) {
				JSONObject jsonchildrenObj = (JSONObject) jsonsigArray.get(i);
				jsonchildrenObj.put(FlowServiceConstants.CLASS,
						jsonchildrenObj.get(FlowServiceConstants._CLASS));
				jsonchildrenObj.remove(FlowServiceConstants._CLASS);
			}
			JSONArray jsonequipArray = (JSONArray) jsonequipObj
					.get(FlowServiceConstants.CHILDREN);
			for (int i = 0; i < jsonequipArray.size(); i++) {
				JSONObject jsonchildrenObj = (JSONObject) jsonequipArray.get(i);
				jsonchildrenObj.put(FlowServiceConstants.CLASS,
						jsonchildrenObj.get(FlowServiceConstants._CLASS));
				jsonchildrenObj.remove(FlowServiceConstants._CLASS);
				JSONArray jsonShelfArray = (JSONArray) jsonchildrenObj
						.get(FlowServiceConstants.CHILDREN);
				for (int j = 0; j < jsonShelfArray.size(); j++) {
					
					JSONObject jsonShelfchildrenObj = (JSONObject) jsonShelfArray.get(j);
					jsonShelfchildrenObj.put(FlowServiceConstants.CLASS,
							jsonShelfchildrenObj.get(FlowServiceConstants._CLASS));
					jsonShelfchildrenObj.remove(FlowServiceConstants._CLASS);
					JSONArray jsonSlotArray = (JSONArray) jsonShelfchildrenObj
							.get(FlowServiceConstants.CHILDREN);
					for (int k = 0; k < jsonSlotArray.size(); k++) {
						JSONObject jsonSlotchildrenObj = (JSONObject) jsonSlotArray.get(k);
						jsonSlotchildrenObj.put(FlowServiceConstants.CLASS,
								jsonSlotchildrenObj.get(FlowServiceConstants._CLASS));
						jsonSlotchildrenObj.remove(FlowServiceConstants._CLASS);
						JSONArray jsonCardArray = (JSONArray) jsonSlotchildrenObj
								.get(FlowServiceConstants.CHILDREN);
						for (int l = 0; l < jsonCardArray.size(); l++) {
							JSONObject jsonCardchildrenObj = (JSONObject) jsonCardArray.get(l);
							jsonCardchildrenObj.put(FlowServiceConstants.CLASS,
									jsonCardchildrenObj.get(FlowServiceConstants._CLASS));
							jsonCardchildrenObj.remove(FlowServiceConstants._CLASS);
							JSONArray jsonPortArray = (JSONArray) jsonCardchildrenObj
									.get(FlowServiceConstants.CHILDREN);
							for (int m = 0; m < jsonPortArray.size(); m++) {
								JSONObject jsonPortchildrenObj = (JSONObject) jsonPortArray.get(m);
								jsonPortchildrenObj.put(FlowServiceConstants.CLASS,
										jsonPortchildrenObj.get(FlowServiceConstants._CLASS));
								jsonPortchildrenObj.remove(FlowServiceConstants._CLASS);
							}
						}
					}
				}
			}
			
			JSONObject jsonAttributesObj = (JSONObject) jsonTopologyObj
					.get(FlowServiceConstants.ATTRIBUTES);
			JSONArray jsonArr = (JSONArray) jsonAttributesObj
					.get(FlowServiceConstants.NODE_ATTRIBUTE);
			for (int i = 0; i < jsonArr.size(); i++) {
				JSONObject jsonchildObj = (JSONObject) jsonArr.get(i);
				logger.info("child object:" + jsonchildObj);
				jsonAttributesObj.remove(FlowServiceConstants.NODE_ATTRIBUTE);
				jsonAttributesObj.put(
						jsonchildObj.get(FlowServiceConstants.NODE_ID),
						jsonchildObj);
				jsonchildObj.remove("nodeId");
				//JsonPath
			}
			logger.info("node attr after replace" + jsonAttributesObj);
			logger.info("createTopologyResponse :: End ---" + jsonObj);
			//logger.info("******"+jsonObj.get(key));
    

			return jsonObj;
		/*} catch (Exception e) {

			logger.error(" Error while reading  topolgy object from cache" + e);

		}*/
		//return jsonObj;
	}
	public JSONObject getNetAssistResponse(String tid) throws Exception {
		logger.info("getNetAssistResponse :: Start"+tid);
		String response = "";
		Map<String,String> params=new HashMap<String,String>();
		params.put("TID", tid);
		response=backEndWrapper.getBackEndService(SvcName.NETASSIST, params, null);		
		JSONParser jsonParser = new JSONParser();
		JSONObject	jsonObject = (JSONObject)jsonParser.parse(response);
		logger.info("getNetAssistResponse  response::"+response);
		logger.info("getNetAssistResponse :: End");
		return jsonObject;
	}
	
	public NetworkElements getInventoryDetails(Map<String,String> inv){
		NetworkElements net = new NetworkElements();
		for (Map.Entry<String, String> entry : inv.entrySet()) {
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
		//		card.setId("");
					if(FlowServiceConstants.PORT_CLASS.toUpperCase().equals(form[0])){
						Port port = new Port();
						port.setId(a[3]);
						card.getPortList().add(port);
						slot.getCardList().add(card);
						shelf.getSlotList().add(slot);
						if(net.getShelfList().size()==0){
						net.getShelfList().add(shelf);
						}else{
							boolean isShelf = true;
							    for(Shelf sh : net.getShelfList()){
								String hid = sh.getId();
								if(hid.equals(a[1])){
								boolean isSlot = true;
								for(Slot sl : sh.getSlotList()){
								String sid = sl.getId();
								if(sid.equals(a[2]) ){
								boolean isCard = true;
								for(Card ca : sl.getCardList()){
									String cid = ca.getId();
									if(cid.equals(a[1]+"-"+a[2])){
										ca.getPortList().add(port);
										isCard = false;
										isSlot=false;
										isShelf=false;
									}
								}						
								    if(isCard){
									    isSlot=false;
									    isShelf=false;
										sl.getCardList().add(card);
									}
							    }
							}
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
                    if(FlowServiceConstants.MODULE_CLASS.toUpperCase().equals(form[0])){
                    	Module module = new Module();
                    	module.setId(a[3]);
						card.getModuleList().add(module);
						slot.getCardList().add(card);
						shelf.getSlotList().add(slot);
						
						if(net.getShelfList().size()==0){
						net.getShelfList().add(shelf);
						}else{
							boolean isShelf = true;
							for(Shelf sh : net.getShelfList()){
								String hid = sh.getId();
								if(hid.equals(a[1])){
									boolean isSlot = true;
									for(Slot sl: sh.getSlotList()){
								String sid = sl.getId();
								if(sid.equals(a[2]) ){
									boolean isCard = true;
									for(Card ca : sl.getCardList()){
									String cid = ca.getId();
									if(cid.equals(a[1]+"-"+a[2])){
										ca.getModuleList().add(module);
										isCard = false;
										isSlot=false;
										isShelf=false;
									}
								}						
									if(isCard){
										isSlot=false;
										isShelf=false;
										sl.getCardList().add(card);
									}
								}
							}
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
			}
            if(null!=form&&form.length==3&&FlowServiceConstants.FAN_CLASS.toUpperCase().equals(form[0])){
            Shelf shelf = new Shelf();
            shelf.setId(a[1]);
            Fan fan = new Fan();
            fan.setId(a[2]);
            shelf.setFan(fan);
            if(net.getShelfList().size()==0){
				net.getShelfList().add(shelf);
				}
            else{
            	boolean isShelf = true;
				   for(Shelf sh : net.getShelfList()){
					   String hid = sh.getId();
					   if(hid.equals(a[1])){
						   String fid = sh.getFan().getId();
						   if(!fid.equals(a[2])){
							   sh.setFan(fan);
						   }
					   }
				   }
				   if(isShelf){
						net.getShelfList().add(shelf);
						}
                  }
           }
            if(null!=form&&form.length==3&&FlowServiceConstants.CARD_CLASS.toUpperCase().equals(form[0])){
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
					boolean isShelf = true;
					   for(Shelf sh : net.getShelfList()){
						String hid = sh.getId();
						if(hid.equals(a[1])){
							boolean isSlot = true;
							for(Slot sl : sh.getSlotList()){
						String sid = sl.getId();
						if(sid.equals(a[2])){
							if(!(hid+"-"+sid).equals(card.getId())){
						   sl.getCardList().add(card);
							}
						   isSlot=false;
						   isShelf = false;
						}
					}
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
 
            /*if(null!=form&&form.length==3&&(FlowServiceConstants.MODULE_CLASS.toUpperCase().equals(form[0])||
             FlowServiceConstants.SLOT_CLASS.toUpperCase().equals(form[0]))){
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
							int slotlength = sh.getSlotList().size();
							boolean isSlot = true;
							for(int j=0;j<slotlength;j++){	
							Slot sl= sh.getSlotList().get(j);
						String sid = sl.getId();
						if(sid.equals(a[2])){
						   isSlot=false;
						   isShelf = false;
						}}
						if(isSlot){
								isShelf=false;
							sh.getSlotList().add(slot);
						}
						}}
					    if(isShelf){
						net.getShelfList().add(shelf);
						}
				}
	       }*/
            
            /*if(null!=form&&form.length==3&&"MODULE".equals(form[0])){
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
	       }*/
			
		}	
		return net;
	
	}
	/*public String  getNetAssistValues(String keyVal,JSONObject netassistresponse) throws Exception {
		
		logger.info("key value with json path::"+keyVal);
		keyVal=keyVal.replace("json(", "");
		String keyValue="";
		if(keyVal.contains("[?"))
		{
			keyVal=keyVal.substring(0, keyVal.length()-1);
			JSONArray array =JsonPath.read(netassistresponse,keyVal);
			keyValue=array.get(0).toString();
		}
		else
		{
			  if(keyVal.endsWith(")"))
			  {
				  keyVal=keyVal.substring(0, keyVal.length()-1);
			keyValue=JsonPath.read(netassistresponse, keyVal);
			  }
			  else
			  {
				  keyValue=keyVal.split("\\)")[1];
			  }
		}
		logger.info("keyValue after replace  json  with value::"+keyValue);
		return keyValue;
	}	*/
	
}