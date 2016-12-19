package com.verizon.delphi.msvcs.model.topology;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;

public class ReadXML {

	private static final Logger logger = Logger.getLogger(ReadXML.class);

	public static String getDeviceType(String input, String TID) throws IOException {
		File file = new File(input);
		JAXBContext jaxbContext;
		String result = "";
		String deviceType = "";
		try {
			jaxbContext = JAXBContext.newInstance(Inventory.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			Object inventory = jaxbUnmarshaller.unmarshal(file);

			for (Site site : ((Inventory) inventory).getSite()) {
				for (Node node : site.getNode()) {
					if (node.getTid() != null && TID != null && node.getTid().equals(TID)) {
						logger.info("TID Value " + node.getTid());
						result = node.getTypeID().getValue();
						if (result.contains("-")) {
							deviceType = result;
						} else {
							String Str1 = result.replace(".", "-");
							String Str2 = Str1.split("-")[0];
							deviceType = Str2;
						}
						logger.info("ModelType is "+deviceType);
					}
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return deviceType;
	}

	public static String getDeviceTypeByXMLString(String xml, String TID) {
		JAXBContext jaxbContext;
		String result = "";
		String deviceType = "";
		try {
			jaxbContext = JAXBContext.newInstance(Inventory.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			StreamSource streamSource = new StreamSource(new StringReader(xml));
			Object inventory = jaxbUnmarshaller.unmarshal(streamSource);

			for (Site site : ((Inventory) inventory).getSite()) {
				for (Node node : site.getNode()) {
					if (node.getTid() != null && TID != null && node.getTid().equals(TID)) {
						logger.info("TID Value " + node.getTid());
						result = node.getTypeID().getValue();
						logger.info("Model Type is " + result);
						if (result.contains("-")) {
							deviceType = result;
						} else {
							String Str1 = result.replace(".", "-");
							String Str2 = Str1.split("-")[0];
							deviceType = Str2;
						}
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return deviceType;
	}

	public static void main(String[] args) throws IOException {
		getDeviceType(args[0], args[1]);
	}

}