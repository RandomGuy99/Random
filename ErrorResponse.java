package com.verizon.delphi.msvcs.util;

import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;

import com.verizon.delphi.msvcs.model.Data;

public class ErrorResponse {

	private static final Logger logger = Logger.getLogger(ErrorResponse.class);

	public void errorResponseToWfm(Data response) {

		HttpURLConnection conn = null;
		String status="";
	//	Data data = new Data();
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Data.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

			// output pretty printed
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			StringWriter sw = new StringWriter();
			jaxbMarshaller.marshal(response, sw);
			String xmlString = sw.toString();
			logger.info("XML is " + xmlString);

			URL url = new URL("SNAKE_TEST_URL");
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestProperty("Content-Length", String.valueOf(xmlString.length()));
			conn.setDoOutput(true);
			byte[] postDataBytes = xmlString.toString().getBytes("UTF-8");
			conn.getOutputStream().write(postDataBytes);

			if (conn.getResponseCode() == 200) {
				status="Response Sent Succesfully to WFM";
			}
			else{
				status="Error in Sending response to WFM";
			}
			logger.info("Response Status is :"+status); 

		} catch (Exception e) {
			logger.error(":::: Exception in the XMLLLL Method ::::" + e.getLocalizedMessage());
		} 
		
	}
}

/*
 * 
 * HttpClient client = new HttpClient(); PostMethod method = new
 * PostMethod("http://10.15.13.154/wfm/wfm"); method.setRequestBody(getData);
 * 
 * int respCode = client.executeMethod(method); if(200==respCode) {
 * jsonObj.put("result","success"); }
 *
 * 
 */
