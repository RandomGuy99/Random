package com.verizon.www.wfm.Snaketest;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.verizon.delphi.msvcs.model.EthernetSnakeTest;

public class SnakeTestResponseService {

	private static final Logger logger = LoggerFactory.getLogger(SnakeTestResponseService.class);

	SnakeTestServiceSOAPQSService service = new SnakeTestServiceSOAPQSService("http://f86sacovn02.ebiz.verizon.com:6001/wfm/service/SnakeTestService?wsdl");

	public void pushEthernetResultsToWFM(EthernetSnakeTest response) throws RemoteException {

		try {
			SnakeTestService sts = service.getSnakeTestServiceSOAPQSPort();
			UPDATEPOSTBERTRESPONSE upbr = sts.updatePostBertsDetails(WfmResponse.createUpdateRequest(response));
			upbr.getSTATUSTYPE();
			upbr.getSTATUSMESSAGE();
			logger.info("Status Type is " + upbr.getSTATUSTYPE() + " and Status Message is " + upbr.getSTATUSMESSAGE()
					+ " after posting Ethernet Stats to Wfm");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void pushSonetResultsToWFM(EthernetSnakeTest response) throws RemoteException {

		try {
			SnakeTestService sts = service.getSnakeTestServiceSOAPQSPort();
			UPDATEPOSTBERTRESPONSE upbr = sts.updatePostBertsDetails(WfmResponse.createUpdateRequest(response));
			upbr.getSTATUSTYPE();
			upbr.getSTATUSMESSAGE();
			logger.info("Status Type is " + upbr.getSTATUSTYPE() + " and Status Message is " + upbr.getSTATUSMESSAGE()
					+ " after posting Sonet results to Wfm");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void postMessageDetails(List<PostMessageList> response) throws RemoteException {

		try {
			SnakeTestService sts = service.getSnakeTestServiceSOAPQSPort();
			POSTRESPONSESTATUS prs = sts.postMessageDetails(WfmResponse.createPostRequest(response));
			prs.getSTATUSTYPE();
			prs.getSTATUSMESSAGE();
			logger.info("Status Type is " + prs.getSTATUSTYPE() + " and Status Message is " + prs.getSTATUSMESSAGE()
					+ " after sending post message details to Wfm");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws RemoteException {
		SnakeTestResponseService snks = new SnakeTestResponseService();
		List<PostMessageList> pmls = new ArrayList<PostMessageList>();
		snks.postMessageDetails(pmls);
//		EthernetSnakeTest pmls = new EthernetSnakeTest();
//		snks.pushSonetResultsToWFM(pmls);
	}
}
