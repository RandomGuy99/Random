package com.verizon.delphi.service.impl;


import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import com.verizon.delphi.model.NeatInfo;
import com.verizon.delphi.model.NeatSourceInfo;
import com.verizon.delphi.model.NetAssistInfo;
import com.verizon.delphi.model.NodeAttribute;
import com.verizon.delphi.model.NodeInfo;
import com.verizon.delphi.model.SignalFlow;
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
		List<String> commandList = new ArrayList<String>();
		List<NodeAttribute> nodeAttributeList = new ArrayList<NodeAttribute>();
		String modelId="";
		String tidVal="";
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
						if (null != aidName && "" != aidName.getValue()) {
							AidsInfo aidInfo = new AidsInfo();
							aidInfo.setAid(aidName.getValue());
							aidInfo.setRateCode("");
							aidList.add(aidInfo);

						}

					} else if (startElement.getName().getLocalPart()
							.equals(FlowServiceConstants.KeyWord_TAG)) {

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

		List<NodeInfo> nodeInfoList = new ArrayList<NodeInfo>();
		List<NodeAttribute> nodeAttributeList = neatInfo.getNodeAttribute();
		List<NodeAttribute> nodeAttrList = new ArrayList<NodeAttribute>();
		NodeInfo children = null;

		NodeAttribute nodeAttribute = null;
		String nodeToken = "";
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
			nodeAttribute.setAidlist(nodeAttr.getAidlist());
			nodeAttribute.setNodeId(nodeToken);
			nodeAttrList.add(nodeAttribute);
		}
		signalFlow.setId(topologyToken);
		signalFlow.setLabel(neatInfo.getTid());
		signalFlow.setChildren(nodeInfoList);
		attributes.setNodeAttribute(nodeAttrList);

		topology.setAttributes(attributes);
		topology.setSignalFlow(signalFlow);
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

			JSONArray jsonArray = (JSONArray) jsonsignalObj
					.get(FlowServiceConstants.CHILDREN);
			for (int i = 0; i < jsonArray.size(); i++) {
				JSONObject jsonchildrenObj = (JSONObject) jsonArray.get(i);
				jsonchildrenObj.put(FlowServiceConstants.CLASS,
						jsonchildrenObj.get(FlowServiceConstants._CLASS));
				jsonchildrenObj.remove(FlowServiceConstants._CLASS);
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