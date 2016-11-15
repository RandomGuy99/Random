package com.verizon.delphi.msvcs.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.verizon.delphi.dms.config.CommonService;
import com.verizon.delphi.dms.exception.CommonServiceException;
import com.verizon.delphi.msvcs.dao.SnakeDao;
import com.verizon.delphi.msvcs.request.BERT;
import com.verizon.delphi.msvcs.request.Data;

@Component
public class SnakeTestValidator {
	private static final Logger logger = LoggerFactory.getLogger(SnakeTestValidator.class);

	@Autowired
	SnakeDao snakeDao;

	@Autowired
	public CommonService commonService;

	public ValidationResponse isvalidRequest(Data request) {
		boolean isValid = true;
		ValidationResponse response = new ValidationResponse();
		StringBuilder message = new StringBuilder();

		if (StringUtils.isBlank(request.getNewSnaketest().getUserid())) {
			message.append("    User ID is missing");
			isValid = false;
		} else if (StringUtils.isBlank(request.getNewSnaketest().getWfmId())) {
			message.append("    WFM ID is missing");
			isValid = false;
		} else if (StringUtils.isBlank(request.getNewSnaketest().getWorktype())) {
			message.append("    WorkType is missing");
			isValid = false;
		}
		/*
		 * Checking the number of BERTS in the request and make sure each BERT
		 * is distinct with no duplicates
		 */

		List<BERT> berts = request.getNewSnaketest().getBERTS().getBERT();
		logger.info("Number of BERTS in the request :" + berts.size());
		if (berts.size() > 0) {
			for (int i = 0; i < berts.size(); i++) {
				BERT firstbert = berts.get(i);
				if (StringUtils.isBlank(((BERT) firstbert).getBERTID())) {
					message.append("    Invalid Request - BERDID is missing" + "\n");
					isValid = false;
				}
				if (StringUtils.isBlank(((BERT) firstbert).getCID())) {
					message.append("    Invalid Request - CID is missing" + "\n");
					isValid = false;
				}
				if (StringUtils.isBlank(((BERT) firstbert).getInterfaceType())) {
					message.append("    InterfaceType is missing" + "\n");
					isValid = false;
				}
				if (StringUtils.isBlank(((BERT) firstbert).getLogicalInterfaceA())) {
					message.append("    Logical Interface A is missing" + "\n");
					isValid = false;
				}
				if (StringUtils.isBlank(((BERT) firstbert).getLogicalInterfaceZ())) {
					message.append("    Logical Interface Z is missing" + "\n");
					isValid = false;
				}
				if (StringUtils.isBlank(((BERT) firstbert).getPhysicalInterfaceA())) {
					message.append("    Physical Interface A is missing" + "\n");
					isValid = false;
				}
				if (StringUtils.isBlank(((BERT) firstbert).getPhysicalInterfaceZ())) {
					message.append("    Physical Interface Z is missing" + "\n");
					isValid = false;
				}
				if (StringUtils.isBlank(((BERT) firstbert).getRouterA())) {
					message.append("    Router A is missing" + "\n");
					isValid = false;
				}
				if (StringUtils.isBlank(((BERT) firstbert).getRouterZ())) {
					message.append("    Router Z is missing" + "\n");
					isValid = false;
				}
				for (int j = i + 1; j < berts.size(); j++) {

					BERT secondbert = berts.get(j);

					if (!StringUtils.isBlank(firstbert.getBERTID()) && !StringUtils.isBlank(secondbert.getBERTID())
							&& firstbert.getBERTID().equals(secondbert.getBERTID())) {
						message.append("    Duplicate BERT ID's was found for " + " " + secondbert.getBERTID()
								+ " comparing with BERDID :" + firstbert.getBERTID() + "\n");
						isValid = false;
					}

					/*
					 * if
					 * (!StringUtils.isBlank(firstbert.getLogicalInterfaceZ())
					 * &&
					 * !StringUtils.isBlank(secondbert.getLogicalInterfaceZ())
					 * && firstbert.getLogicalInterfaceZ().equals(secondbert.
					 * getLogicalInterfaceZ())) { message.append(
					 * "    Duplicate Logical Interface Z's was found " + " " +
					 * secondbert.getLogicalInterfaceZ() + " for BERDID :" +
					 * secondbert.getBERTID() + " comparing with BERDID :" +
					 * firstbert.getBERTID() + "\n"); isValid = false; }
					 * 
					 * if
					 * (!StringUtils.isBlank(firstbert.getPhysicalInterfaceA())
					 * &&
					 * !StringUtils.isBlank(secondbert.getPhysicalInterfaceA())
					 * && firstbert.getPhysicalInterfaceA().equals(secondbert.
					 * getPhysicalInterfaceA())) { message.append(
					 * "    Duplicate Physical Interface A's was found " + " " +
					 * secondbert.getPhysicalInterfaceA() + " for BERDID :" +
					 * secondbert.getBERTID() + " comparing with BERDID :" +
					 * firstbert.getBERTID() + "\n"); isValid = false; }
					 * 
					 * if
					 * (!StringUtils.isBlank(firstbert.getLogicalInterfaceA())
					 * &&
					 * !StringUtils.isBlank(secondbert.getLogicalInterfaceA())
					 * && firstbert.getLogicalInterfaceA().equals(secondbert.
					 * getLogicalInterfaceA())) { message.append(
					 * "    Duplicate Logical Interface A's was found " + " " +
					 * secondbert.getLogicalInterfaceA() + " for BERDID :" +
					 * secondbert.getBERTID() + " comparing with BERDID :" +
					 * firstbert.getBERTID() + "\n"); isValid = false; }
					 * 
					 * if
					 * (!StringUtils.isBlank(firstbert.getPhysicalInterfaceZ())
					 * &&
					 * !StringUtils.isBlank(secondbert.getPhysicalInterfaceZ())
					 * && firstbert.getPhysicalInterfaceZ().equals(secondbert.
					 * getPhysicalInterfaceZ())) { message.append(
					 * "    Duplicate Physical Interface Z's was found " + " " +
					 * secondbert.getPhysicalInterfaceZ() + " for BERDID :" +
					 * secondbert.getBERTID() + " comparing with BERDID :" +
					 * firstbert.getBERTID() + "\n"); isValid = false; }
					 */

					if (!StringUtils.isBlank(firstbert.getCID()) && !StringUtils.isBlank(secondbert.getCID())
							&& firstbert.getCID().equals(secondbert.getCID())) {
						message.append("    Duplicate CID's was found " + " " + secondbert.getCID() + " for BERDID :"
								+ secondbert.getBERTID() + " comparing with BERDID :" + firstbert.getBERTID() + "\n");
						isValid = false;
					}
					if (firstbert.getRouterA().equals(secondbert.getRouterA())
							|| firstbert.getRouterA().equals(secondbert.getRouterZ())
							|| firstbert.getRouterZ().equals(secondbert.getRouterA())
							|| firstbert.getRouterZ().equals(secondbert.getRouterZ())) {

						if (firstbert.getLogicalInterfaceA().equals(secondbert.getLogicalInterfaceZ())
								|| firstbert.getLogicalInterfaceZ().equals(secondbert.getLogicalInterfaceA())
								|| firstbert.getLogicalInterfaceA().equals(secondbert.getLogicalInterfaceA())
								|| firstbert.getLogicalInterfaceZ().equals(secondbert.getLogicalInterfaceZ())) {
							isValid = false;
							message.append("Duplicate Logical Interfaces were found " + "\n");
						}
						
						if (firstbert.getPhysicalInterfaceA().equals(secondbert.getPhysicalInterfaceZ())
								|| firstbert.getPhysicalInterfaceZ().equals(secondbert.getPhysicalInterfaceA())
								|| firstbert.getPhysicalInterfaceA().equals(secondbert.getPhysicalInterfaceA())
								|| firstbert.getPhysicalInterfaceZ().equals(secondbert.getPhysicalInterfaceZ())) {
							isValid = false;
							message.append("Duplicate Physical Interfaces were found " + "\n");
						}
					}
				}
			}

		} else {
			logger.info("No BERTS were found");
			message.append("No BERTS were found" + "\n");
			isValid = false;
		}
		response.setStatus(isValid);
		if (!isValid) {
			logger.info("Validation Failed :" + message.toString());
			response.setErrorMessage("Invalid Request:: Validation Failed");
			response.setErrorDescription(message.toString());
		}

		return response;
	}
	/*
	 * Method to check if the Router Type is in DB, if not get it from the
	 * topology. If unable to determine the routerType, return the error
	 * response to WFM.
	 */

	public ValidationResponse checkModelType(Data request) throws CommonServiceException, WebServiceException {
		ValidationResponse response = new ValidationResponse();
		String routerA = "";
		String routerZ = "";
		String modelTypeA = "";
		String modelTypeZ = "";
		boolean isValid = true;

		Map<String, String> model = new HashMap<String, String>();
		StringBuilder message = new StringBuilder();
		List<BERT> berts = request.getNewSnaketest().getBERTS().getBERT();
		for (int i = 0; i < berts.size(); i++) {
			BERT bertList = berts.get(i);
			routerA = bertList.getRouterA();
			routerZ = bertList.getRouterZ();

			if (!snakeDao.getRouterType(routerA.trim())) {
				logger.info("RouterType doesn't exist in database.Getting Topology....");
				try {
					// String xml = commonService.getTopology("abc", false,
					// bertList.getCID(), "kadiyve");
					// modelTypeA = ReadXML.getDeviceTypeByXMLString(xml,
					// routerA.trim());
					modelTypeA = ReadXML.getDeviceType("C://SourceXML//xml.xml ", routerA.trim());
					if (StringUtils.isBlank((modelTypeA))) {
						response.setStatus(false);
						response.setErrorMessage("Error in processing the request");
						response.setErrorDescription("Can't find the Device Type for routerA--" + routerA + " for CID="
								+ bertList.getCID() + "\n");
						return response;
					} else {
						model.put(routerA, modelTypeA);
					}
				} catch (Exception e) {
					logger.error("Error in finding the DeviceType ");
					throw new WebServiceException("Error in finding the DeviceType " + e.getMessage());
				}
			}

			if (!snakeDao.getRouterType(routerZ.trim())) {
				logger.info("RouterType doesn't exist in database.Getting Topology....");
				try {
					// String xmlfile = commonService.getTopology("abc", false,
					// bertList.getCID(), "kadiyve");
					// modelTypeZ = ReadXML.getDeviceTypeByXMLString(xmlfile,
					// routerZ);
					modelTypeZ = ReadXML.getDeviceType("C://SourceXML//xml.xml ", routerZ);
					if (StringUtils.isBlank((modelTypeZ))) {
						response.setStatus(false);
						response.setErrorMessage("Error in processing the request");
						response.setErrorDescription("Can't find the Device Type for routerZ--" + routerZ + " for CID="
								+ bertList.getCID() + "\n");
						return response;
					} else {
						model.put(routerZ, modelTypeZ);
					}
				} catch (Exception e) {
					logger.error("Error in finding the DeviceType ");
					throw new WebServiceException("Error in finding the DeviceType " + e.getMessage());
				}
			}
		}
		response.setStatus(isValid);
		if (model.size() > 0 && isValid) {
			if (!snakeDao.insertRouterType(model)) {
				response.setStatus(false);
				message.append(":::: Error in inserting RouterType into database ::::");
				response.setErrorMessage("Error in processing the request");
				response.setErrorDescription(message.toString());
				return response;
			}
		}
		response = checkBERT(request);

		return response;
	}

	@SuppressWarnings("unchecked")
	public ValidationResponse checkBERT(Data request) throws CommonServiceException, WebServiceException {
		ValidationResponse response = new ValidationResponse();
		BERT bertDetails = new BERT();
		StringBuilder message = new StringBuilder();
		List<String> list = new ArrayList<String>();
		List<BERT> bertList = request.getNewSnaketest().getBERTS().getBERT();
		if (bertList.size() > 0) {
			for (int i = 0; i < bertList.size(); i++) {
				BERT firstbert = bertList.get(i);
				String testId = snakeDao.isBertActive(firstbert.getBERTID());
				if (!StringUtils.isEmpty(testId)) {
					response.setStatus(false);
					message.append(":::: BERT : " + firstbert.getBERTID() + " is in active state with TestID " + testId
							+ " ::::");
					response.setErrorMessage("Error in processing the request");
					response.setErrorDescription(message.toString());
					return response;
				}
			}
			for (int i = 0; i < bertList.size(); i++) {
				BERT firstbert = bertList.get(i);
				String bertId = firstbert.getBERTID();
				if (!snakeDao.isBertIdExist(bertId)) {
					list = snakeDao.insertBert(firstbert, list);
				} else {
					boolean isSame = true;
					bertDetails = snakeDao.getBertDetails(bertId);
					BERT bert = new BERT();
					logger.info(":::: Comparing the input request with existing BERT ::::");
					bert.setBERTID(firstbert.getBERTID());
					if (!bertDetails.getCID().equals(firstbert.getCID())) {
						isSame = false;
						bert.setCID(firstbert.getCID());
					} else {
						bert.setCID(bertDetails.getCID());
					}
					if (!bertDetails.getCLO().equals(firstbert.getCLO())) {
						isSame = false;
						bert.setCLO(firstbert.getCLO());
					} else {
						bert.setCLO(bertDetails.getCLO());
					}
					if (!bertDetails.getInterfaceType().equals(firstbert.getInterfaceType())) {
						isSame = false;
						bert.setInterfaceType(firstbert.getInterfaceType());
					} else {
						bert.setInterfaceType(bertDetails.getInterfaceType());
					}
					if (!bertDetails.getRouterA().equals(firstbert.getRouterA())) {
						isSame = false;
						bert.setRouterA(firstbert.getRouterA());
					} else {
						bert.setRouterA(bertDetails.getRouterA());
					}
					if (!bertDetails.getPhysicalInterfaceA().equals(firstbert.getPhysicalInterfaceA())) {
						isSame = false;
						bert.setPhysicalInterfaceA(firstbert.getPhysicalInterfaceA());
					} else {
						bert.setPhysicalInterfaceA(bertDetails.getPhysicalInterfaceA());
					}
					if (!bertDetails.getLogicalInterfaceZ().equals(firstbert.getLogicalInterfaceZ())) {
						isSame = false;
						bert.setLogicalInterfaceZ(firstbert.getLogicalInterfaceZ());
					} else {
						bert.setLogicalInterfaceZ(bertDetails.getLogicalInterfaceZ());
					}
					if (!bertDetails.getRouterZ().equals(firstbert.getRouterZ())) {
						isSame = false;
						bert.setRouterZ(firstbert.getRouterZ());
					} else {
						bert.setRouterZ(bertDetails.getRouterZ());
					}
					if (!bertDetails.getPhysicalInterfaceZ().equals(firstbert.getPhysicalInterfaceZ())) {
						isSame = false;
						bert.setPhysicalInterfaceZ(firstbert.getPhysicalInterfaceZ());
					} else {
						bert.setPhysicalInterfaceZ(bertDetails.getPhysicalInterfaceZ());
					}
					if (!bertDetails.getLogicalInterfaceA().equals(firstbert.getLogicalInterfaceA())) {
						isSame = false;
						bert.setLogicalInterfaceA(firstbert.getLogicalInterfaceA());
					} else {
						bert.setLogicalInterfaceA(bertDetails.getLogicalInterfaceA());
					}
					bert.setVersion(bertDetails.getVersion());
					if (!isSame) {
						list = snakeDao.updateBertDetails(bert, list);
					}
				}
			}
		}
		if (list.size() > 0) {
			logger.info(":::: Getting all the BERT_ID's inserted in the database ::::");
			response = generateTestId(request, list);
		}
		return response;
	}

	public ValidationResponse generateTestId(Data request, List<String> list) throws WebServiceException {
		ValidationResponse response = new ValidationResponse();
		String testId = snakeDao.inserBertDetails(request);
		if (StringUtils.isNotBlank(testId)) {
			if (!snakeDao.insertTestID(testId, list)) {
				response.setStatus(false);
				response.setErrorMessage("Error in processing the request");
				response.setErrorDescription("::::::Failed to insert testId ::::::");
				return response;
			}
		}
		
		response.setTestid(testId);
		response.setStatus(true);
		return response;
	}
}