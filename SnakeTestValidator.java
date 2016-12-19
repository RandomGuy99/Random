package com.verizon.delphi.msvcs.validate;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.verizon.delphi.dms.config.CommonService;
import com.verizon.delphi.dms.exception.CommonServiceException;
import com.verizon.delphi.msvcs.dao.SnakeDao;
import com.verizon.delphi.msvcs.model.BERT;
import com.verizon.delphi.msvcs.model.Data;
import com.verizon.delphi.msvcs.model.NewSnaketest;
import com.verizon.delphi.msvcs.model.topology.ReadXML;
import com.verizon.delphi.msvcs.service.SnakeTestService;
import com.verizon.delphi.msvcs.util.WebServiceException;

@Component
public class SnakeTestValidator {
	private static final Logger logger = LoggerFactory.getLogger(SnakeTestValidator.class);

	@Autowired
	SnakeDao snakeDao;

	@Autowired
	public CommonService commonService;
	
	@Autowired
	SnakeTestService snakeTestService;
	
	public void firstLevelValidation(Data data){
		NewSnaketest snakeRequest = data.getNewSnaketest();
		isValidRequest(snakeRequest);
		try {
			if (snakeRequest.isStatus()) {
				NewSnaketest response = evalRouterType(snakeRequest);
				if (response.isStatus()) {
					response = createBertIfNotActive(snakeRequest);
				}
			}
			
		} catch (Exception ex) {
			snakeRequest.setStatus(false);
			logger.error("Error in checkModelType", ex);
			snakeRequest.getError().add(ex.getMessage());
		}		
	}
   	
	public void isValidRequest(NewSnaketest snakeRequest) {
		boolean isValid = true;
		NewSnaketest response = snakeRequest;
		
		if (StringUtils.isBlank(snakeRequest.getUserid())) {
			response.getError().add("User ID is missing");
			isValid = false;
		} else if (StringUtils.isBlank(snakeRequest.getWfmId())) {
			response.getError().add("WFM ID is missing");
			isValid = false;
		} else if (StringUtils.isBlank(snakeRequest.getWorktype())) {
			response.getError().add("WorkType is missing");
			isValid = false;
		}
		/*
		 * Checking the number of BERTS in the request and make sure each BERT
		 * is distinct with no duplicates
		 */

		List<BERT> berts = snakeRequest.getBERTS().getBERT();
		logger.info("Number of BERTS in the request :" + berts.size());
		if (berts.size() > 0) {
			for (int i = 0; i < berts.size(); i++) {
				BERT firstbert = berts.get(i);
				if (StringUtils.isBlank(((BERT) firstbert).getBERT())) {
					response.getError().add("Invalid Request - BERTID is missing");
					isValid = false;
				}
				if (StringUtils.isBlank(((BERT) firstbert).getCID())) {
					response.getError().add("Invalid Request - CID is missing");
					isValid = false;
				}
				if (StringUtils.isBlank(((BERT) firstbert).getInterfaceType())) {
					response.getError().add("InterfaceType is missing");
					isValid = false;
				}
				if (StringUtils.isBlank(((BERT) firstbert).getLogicalInterfaceA())) {
					response.getError().add("Logical Interface A is missing");
					isValid = false;
				}
				if (StringUtils.isBlank(((BERT) firstbert).getLogicalInterfaceZ())) {
					response.getError().add("Logical Interface Z is missing");
					isValid = false;
				}
				if (StringUtils.isBlank(((BERT) firstbert).getPhysicalInterfaceA())) {
					response.getError().add("Physical Interface A is missing");
					isValid = false;
				}
				if (StringUtils.isBlank(((BERT) firstbert).getPhysicalInterfaceZ())) {
					response.getError().add("Physical Interface Z is missing");
					isValid = false;
				}
				if (StringUtils.isBlank(((BERT) firstbert).getRouterA())) {
					response.getError().add("Router A is missing");
					isValid = false;
				}
				if (StringUtils.isBlank(((BERT) firstbert).getRouterZ())) {
					response.getError().add("Router Z is missing");
					isValid = false;
				}
				for (int j = i + 1; j < berts.size(); j++) {

					BERT secondbert = berts.get(j);

					if (!StringUtils.isBlank(firstbert.getBERT()) && !StringUtils.isBlank(secondbert.getBERT())
							&& firstbert.getBERT().equals(secondbert.getBERT())) {
						response.getError().add("    Duplicate BERT ID's was found for " + " " + secondbert.getBERT()
								+ " comparing with BERDID :" + firstbert.getBERT());
						isValid = false;
					}

					if (!StringUtils.isBlank(firstbert.getCID()) && !StringUtils.isBlank(secondbert.getCID())
							&& firstbert.getCID().equals(secondbert.getCID())) {
						response.getError()
								.add("    Duplicate CID's was found " + " " + secondbert.getCID() + " for BERDID :"
										+ secondbert.getBERT() + " comparing with BERDID :" + firstbert.getBERT());
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
							response.getError().add("Duplicate Logical Interfaces were found ");
						}

						if (firstbert.getPhysicalInterfaceA().equals(secondbert.getPhysicalInterfaceZ())
								|| firstbert.getPhysicalInterfaceZ().equals(secondbert.getPhysicalInterfaceA())
								|| firstbert.getPhysicalInterfaceA().equals(secondbert.getPhysicalInterfaceA())
								|| firstbert.getPhysicalInterfaceZ().equals(secondbert.getPhysicalInterfaceZ())) {
							isValid = false;
							response.getError().add("Duplicate Physical Interfaces were found ");
						}
					}
				}
			}

		} else {
			logger.info("No BERTS were found");
			response.getError().add("No BERTS were found");
			isValid = false;
		}
		response.setStatus(isValid);
		if (!isValid) {
			logger.info("Validation Failed :" + response.getError().toString());
		}
	}
	/*
	 * Method to check if the Router Type is in DB, if not get it from the
	 * topology. If unable to determine the routerType, return the error
	 * response to WFM.
	 */

	public NewSnaketest evalRouterType(NewSnaketest request) throws CommonServiceException, WebServiceException {
//		NewSnaketest response = new NewSnaketest();
		NewSnaketest response = request;
		String routerA = "";
		String routerZ = "";
		String modelTypeA = "";
		String modelTypeZ = "";
		String routerType = "";
		boolean isValid = true;
		List<BERT> bertList = request.getBERTS().getBERT();
		for (BERT bert : bertList) {
			routerA = bert.getRouterA();
			routerZ = bert.getRouterZ();

			routerType = snakeDao.getRouterType(routerA.trim());
			if (StringUtils.isBlank(routerType)) {
				logger.info("RouterType doesn't exist in database.Getting Topology....");
				try {
					 String xml = commonService.getTopology("abc", false,
							 bert.getCID(), "kadiyve");
					 modelTypeA = ReadXML.getDeviceTypeByXMLString(xml,
					 routerA.trim());
				//	modelTypeA = ReadXML.getDeviceType("C://SourceXML//xml.xml ", routerA.trim());
					if (StringUtils.isBlank((modelTypeA))) {
						response.setStatus(false);
						response.getError().add("Can't find the Device Type for routerA--" + routerA + " for CID="
								+ bert.getCID() + "\n");
						return response;
					} else {
						bert.setModelRouterA(modelTypeA);
						snakeDao.insertRouterType(routerA, modelTypeA);
					}
				} catch (Exception e) {
					logger.error("Error in finding the DeviceType ");
					throw new WebServiceException("Error in finding the DeviceType " + e.getMessage());
				}
			} else {
				bert.setModelRouterA(routerType);
			}

			routerType = snakeDao.getRouterType(routerZ.trim());
			if (StringUtils.isBlank(routerType)) {
				logger.info("RouterType doesn't exist in database.Getting Topology....");
				try {
					 String xmlfile = commonService.getTopology("abc", false,
							 bert.getCID(), "kadiyve");
					 modelTypeZ = ReadXML.getDeviceTypeByXMLString(xmlfile,
					 routerZ);
				//	modelTypeZ = ReadXML.getDeviceType("C://SourceXML//xml.xml ", routerZ);
					if (StringUtils.isBlank((modelTypeZ))) {
						response.setStatus(false);
						response.getError().add("Can't find the Device Type for routerZ--" + routerZ + " for CID="
								+ bert.getCID() + "\n");
						return response;
					} else {
						bert.setModelRouterZ(modelTypeZ);
						snakeDao.insertRouterType(routerZ, modelTypeZ);
					}
				} catch (Exception e) {
					logger.error("Error in finding the DeviceType ");
					throw new WebServiceException("Error in finding the DeviceType " + e.getMessage());
				}
			} else {
				bert.setModelRouterZ(routerType);
			}
		}
		response.setStatus(isValid);
		return response;
	}

	public NewSnaketest createBertIfNotActive(NewSnaketest request) throws CommonServiceException, WebServiceException {
		List<BERT> bertList = request.getBERTS().getBERT();
		if (bertList.size() > 0) {
			for (BERT bert : bertList) {
				String testId = snakeDao.isBertActive(bert.getBERT());
				if (!StringUtils.isEmpty(testId)) {
					request.setStatus(false);
					request.getError()
							.add("BERT : " + bert.getBERT() + " is in active state with TestID " + testId + "");
					logger.info("BERT : " + bert.getBERT() + " is in active state with TestID " + testId + "");
					return request;
				}
			}
			for (BERT bert : bertList) {
				String bertName = bert.getBERT();
				if (!snakeDao.isBertExist(bertName)) {
					String bertId = snakeDao.insertBert(bert, false);
					bert.setBertid(bertId);
				} else {
					boolean isSame = true;
					BERT existingBert = snakeDao.getBertDetails(bertName);

					logger.info(":::: Comparing the input request with existing BERT ::::");
					if (!existingBert.getCID().equals(bert.getCID())) {
						isSame = false;
					}
					if (!existingBert.getCLO().equals(bert.getCLO())) {
						isSame = false;
					}
					if (!existingBert.getInterfaceType().equals(bert.getInterfaceType())) {
						isSame = false;
					}
					if (!existingBert.getRouterA().equals(bert.getRouterA())) {
						isSame = false;
					}
					if (!existingBert.getPhysicalInterfaceA().equals(bert.getPhysicalInterfaceA())) {
						isSame = false;
					}
					if (!existingBert.getLogicalInterfaceZ().equals(bert.getLogicalInterfaceZ())) {
						isSame = false;
					}
					if (!existingBert.getRouterZ().equals(bert.getRouterZ())) {
						isSame = false;
					}
					if (!existingBert.getPhysicalInterfaceZ().equals(bert.getPhysicalInterfaceZ())) {
						isSame = false;
					}
					if (!existingBert.getLogicalInterfaceA().equals(bert.getLogicalInterfaceA())) {
						isSame = false;
					}
					if (!isSame) {
						String bertId = snakeDao.insertBert(existingBert, true);
						bert.setBertid(bertId);
					} else {
						bert.setBertid(existingBert.getBertid());
					}
				}
			}
		}
		if (bertList.size() > 0) {
			logger.info(":::: Getting all the BERT_ID's inserted in the database ::::");
			mapSnakeTestIdToBertId(request, bertList);
		}
		return request;
	}

	public NewSnaketest mapSnakeTestIdToBertId(NewSnaketest request, List<BERT> bertList)
			throws WebServiceException {
		String testId = snakeDao.createSnakeTest(request);
		if (StringUtils.isNotBlank(testId)) {
			if (!snakeDao.createBundle(testId, bertList)) {
				request.setStatus(false);
				request.getError().add("::::::Failed to insert testId ::::::");
				return request;
			}
		}
		request.setTestid(testId);
		request.setStatus(true);
		return request;
	}
}