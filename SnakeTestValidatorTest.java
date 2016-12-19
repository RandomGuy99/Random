package com.verizon.delphi.msvcs.validate;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.util.StringUtils;

import com.verizon.delphi.config.DbWrapper;
import com.verizon.delphi.dms.config.CommonService;
import com.verizon.delphi.dms.exception.CommonServiceException;
import com.verizon.delphi.msvcs.dao.SnakeDao;
import com.verizon.delphi.msvcs.dao.impl.SnakeDaoImpl;
import com.verizon.delphi.msvcs.model.BERT;
import com.verizon.delphi.msvcs.model.BERTS;
import com.verizon.delphi.msvcs.model.Data;
import com.verizon.delphi.msvcs.model.NewSnaketest;
import com.verizon.delphi.msvcs.model.topology.Inventory;
import com.verizon.delphi.msvcs.model.topology.Node;
import com.verizon.delphi.msvcs.model.topology.ReadXML;
import com.verizon.delphi.msvcs.model.topology.Site;
import com.verizon.delphi.msvcs.resource.TestInputProvider;
import com.verizon.delphi.msvcs.util.WebServiceException;

@RunWith(MockitoJUnitRunner.class)
public class SnakeTestValidatorTest {

	@Mock
	private DbWrapper dbWrapper;

	@Mock
	private CommonService commonService;
	
	@InjectMocks
	@Spy
	private SnakeTestValidator snakeValidator = new SnakeTestValidator();

	@InjectMocks
	@Spy
	private SnakeDao snakeDao = new SnakeDaoImpl();

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	
	  @Test public void getTopologyTest() throws SQLException,CommonServiceException, WebServiceException, JAXBException {
	  
	  NewSnaketest request = TestInputProvider.getClientRequest().getNewSnaketest(); 
	  SqlRowSet rows = Mockito.mock(SqlRowSet.class);
	  JAXBContext jax = Mockito.mock(JAXBContext.class);
	  Unmarshaller unm = Mockito.mock(Unmarshaller.class);
	  StreamSource stm = Mockito.mock(StreamSource.class);
	  Site site = Mockito.mock(Site.class);
	  Node node = Mockito.mock(Node.class);
	  Inventory inv = Mockito.mock(Inventory.class);
	  ReadXML xml = Mockito.mock(ReadXML.class);
	  String TID="BTHYPAB";
	  String deviceType="Tellabs7100";
	  when(rows.getString("ROUTERTYPE")).thenReturn("");
	  when(rows.next()).thenReturn(true, false);
	  when(inv.getSite()).thenReturn(Arrays.asList());
	  when(site.getNode()).thenReturn(Arrays.asList());
	  when(jax.createUnmarshaller()).thenReturn(unm);
	  when(unm.unmarshal(stm)).thenReturn(Object.class);
	  when(node.getTid()).thenReturn(deviceType);
//	  when(TID).thenReturn(TID);
	  when(node.getTid().equals(TID)).thenReturn(null);
	 
	  when(dbWrapper.queryForRowSet(Mockito.anyString(),Mockito.any(Object[].class))).thenReturn(rows);
	  when(commonService.getTopology(Mockito.anyString(), Mockito.anyBoolean(),Mockito.anyString(), Mockito.anyString())).thenReturn(Mockito.anyString());
	  when(ReadXML.getDeviceTypeByXMLString(Mockito.anyString(),Mockito.anyString())).thenReturn(Mockito.anyString()); 
	  NewSnaketest resp=snakeValidator.evalRouterType(request);
	  assertTrue(!resp.getError().isEmpty());
	  
	  }
	 

	/*
	  @Test 
	  public void evalRouterTypeTest() throws SQLException, CommonServiceException, WebServiceException{
	  NewSnaketest request = TestInputProvider.getClientRequest().getNewSnaketest(); 
	  SqlRowSet rows = Mockito.mock(SqlRowSet.class);
	  when(rows.getString("ROUTERTYPE")).thenReturn("routerNameA","routerNameZ"); 
	  when(rows.next()).thenReturn(true,false,true,false);
	  when(dbWrapper.queryForRowSet(Mockito.anyString(),Mockito.any(Object[].class))).thenReturn(rows,rows);
	  //thrown.expect(WebServiceException.class);
	  //thrown.expectMessage(startsWith("Error in"));
	  snakeValidator.evalRouterType(request);
	  }
	  
	  @Test
	  public void test() throws SQLException, CommonServiceException, WebServiceException{
	  NewSnaketest request = TestInputProvider.getClientRequest().getNewSnaketest(); 
	  SqlRowSet rows = Mockito.mock(SqlRowSet.class);	  
	  when(dbWrapper.queryForRowSet(Mockito.anyString(),Mockito.any(Object[].class))).thenReturn(rows);
	  thrown.expect(WebServiceException.class);
	  snakeValidator.evalRouterType(request);
	  }

	@Test // Checks for Active Bert and return error if found
	public void checkforActiveBertTest() throws SQLException, CommonServiceException, WebServiceException {
		NewSnaketest snkt = TestInputProvider.getClientRequest().getNewSnaketest();
		SqlRowSet rows = Mockito.mock(SqlRowSet.class);
		when(rows.getString("TESTID")).thenReturn("1234");
		when(rows.next()).thenReturn(true, false);
		when(dbWrapper.queryForRowSet(Mockito.anyString(), Mockito.any(Object[].class))).thenReturn(rows);
		NewSnaketest resp = snakeValidator.createBertIfNotActive(snkt);
		assertNotNull(resp);
		assertTrue(!resp.getError().isEmpty());
	}

	@Test // ===== IF NOT SAME BERT COMES =====
	public void ifBertNotExistTest() throws SQLException, CommonServiceException, WebServiceException {
		NewSnaketest snkt = TestInputProvider.getClientRequest().getNewSnaketest();
		SqlRowSet rows = Mockito.mock(SqlRowSet.class);
		when(rows.getString("TESTID")).thenReturn("");
		when(rows.getString("BERT")).thenReturn("E-LAN1234");
		when(rows.getString("CID")).thenReturn("FTWSPAW30WANXSU");
		when(rows.getString("CLO")).thenReturn("1281");
		when(rows.getString("INTERFACE_TYPE")).thenReturn("10Giga");
		when(rows.getString("ROUTER_A")).thenReturn("10.0.0.0.3");
		when(rows.getString("PHYSICAL_INTERFACE_A")).thenReturn("26/N/A/Mask");
		when(rows.getString("LOGICAL_INTERFACE_A")).thenReturn("35/N/A/Mask");
		when(rows.getString("ROUTER_Z")).thenReturn("10.0.0.0.5");
		when(rows.getString("PHYSICAL_INTERFACE_Z")).thenReturn("26/N/A/Mask.0");
		when(rows.getString("LOGICAL_INTERFACE_Z")).thenReturn("32/N/A/Mask.0");
		when(rows.getString("BERT_VERSION")).thenReturn("1");
		when(rows.getString("BERT_ID")).thenReturn("E-LAN1234_1");
		when(rows.next()).thenReturn(true, false, true, false, true, false);
		when(dbWrapper.queryForRowSet(Mockito.anyString(), Mockito.any(Object[].class))).thenReturn(rows, rows);
		when(dbWrapper.update(Mockito.anyString(), Mockito.anyMap())).thenReturn(1).thenReturn(1);
		NewSnaketest resp = snakeValidator.createBertIfNotActive(snkt);
		assertNotNull(resp);
		assertTrue(!StringUtils.isEmpty(resp.getTestid()));
	}

	@Test // ===== IF SAME BERT COMES =====
	public void ifBertExistTest() throws SQLException, CommonServiceException, WebServiceException {
		NewSnaketest snkt = TestInputProvider.getClientRequest().getNewSnaketest();
		SqlRowSet rows = Mockito.mock(SqlRowSet.class);
		when(rows.getString("TESTID")).thenReturn("");
		when(rows.getString("BERT")).thenReturn("E-LAN1234");
		when(rows.getString("CID")).thenReturn("FTWSPAFW30WANXSU");
		when(rows.getString("CLO")).thenReturn("1221");
		when(rows.getString("INTERFACE_TYPE")).thenReturn("100Giga");
		when(rows.getString("ROUTER_A")).thenReturn("10.0.0.0.1");
		when(rows.getString("PHYSICAL_INTERFACE_A")).thenReturn("22/N/A/Mask");
		when(rows.getString("LOGICAL_INTERFACE_A")).thenReturn("34/N/A/Mask");
		when(rows.getString("ROUTER_Z")).thenReturn("10.0.0.0.7");
		when(rows.getString("PHYSICAL_INTERFACE_Z")).thenReturn("22/N/A/Mask.0");
		when(rows.getString("LOGICAL_INTERFACE_Z")).thenReturn("34/N/A/Mask.0");
		when(rows.getString("BERT_VERSION")).thenReturn("1");
		when(rows.getString("BERT_ID")).thenReturn("E-LAN1234_1");
		when(rows.next()).thenReturn(true, false, true, false, true, false);
		when(dbWrapper.queryForRowSet(Mockito.anyString(), Mockito.any(Object[].class))).thenReturn(rows, rows);
		when(dbWrapper.update(Mockito.anyString(), Mockito.anyMap())).thenReturn(1).thenReturn(1);
		NewSnaketest resp = snakeValidator.createBertIfNotActive(snkt);
		assertNotNull(resp);
		assertTrue(!StringUtils.isEmpty(resp.getTestid()));
	}

	@Test // If new BERT request comes
	public void ifNewBertTest() throws CommonServiceException, WebServiceException, SQLException {
		NewSnaketest snkt = TestInputProvider.getClientRequest().getNewSnaketest();
		SqlRowSet rows = Mockito.mock(SqlRowSet.class);
		when(rows.getString("TESTID")).thenReturn("");
		when(rows.next()).thenReturn(false, false, true, false, true, false);
		when(dbWrapper.queryForRowSet(Mockito.anyString(), Mockito.any(Object[].class))).thenReturn(rows, rows);
		when(dbWrapper.update(Mockito.anyString(), Mockito.anyMap())).thenReturn(1).thenReturn(1);
		NewSnaketest resp = snakeValidator.createBertIfNotActive(snkt);
		assertNotNull(resp);
	}

	
	 * @Test public void insertRouterTypeTest() throws SQLException,
	 * CommonServiceException, WebServiceException{
	 * when(dbWrapper.update(Mockito.anyString(),Mockito.any(Object[].class))).
	 * thenReturn(1); //thrown.expect(WebServiceException.class);
	 * //thrown.expectMessage(startsWith("Error in"));
	 * snakeDao.insertRouterType("hostname","routerName"); // assertNotNull() }
	 

	@Test
	public void firstLevelValidationTest() {

		Data data = TestInputProvider.getClientRequest();
		data.getNewSnaketest().setUserid(null);
		snakeValidator.firstLevelValidation(data);
		assertTrue(data.getNewSnaketest().getError().contains("User ID is missing"));
		data.getNewSnaketest().setUserid("KADIYVE");

		data.getNewSnaketest().setWfmId(null);
		snakeValidator.firstLevelValidation(data);
		assertTrue(data.getNewSnaketest().getError().contains("WFM ID is missing"));
		data.getNewSnaketest().setWfmId("5634");

		data.getNewSnaketest().setWorktype(null);
		snakeValidator.firstLevelValidation(data);
		assertTrue(data.getNewSnaketest().getError().contains("WorkType is missing"));
		data.getNewSnaketest().setWorktype("Circuit Type");

		List<BERT> berts = data.getNewSnaketest().getBERTS().getBERT();
		BERT bert = berts.get(0);

		bert.setBERT(null);
		snakeValidator.firstLevelValidation(data);
		assertTrue(data.getNewSnaketest().getError().contains("Invalid Request - BERTID is missing"));
		bert.setBERT("ELAN-1298");

		bert.setCID(null);
		snakeValidator.firstLevelValidation(data);
		assertTrue(data.getNewSnaketest().getError().contains("Invalid Request - CID is missing"));
		bert.setCID("CIRCUIT/ID/093");

		bert.setInterfaceType(null);
		snakeValidator.firstLevelValidation(data);
		assertTrue(data.getNewSnaketest().getError().contains("InterfaceType is missing"));
		bert.setInterfaceType("sample");

		bert.setLogicalInterfaceA(null);
		snakeValidator.firstLevelValidation(data);
		assertTrue(data.getNewSnaketest().getError().contains("Logical Interface A is missing"));
		bert.setLogicalInterfaceA("11/AB/Mask");

		bert.setLogicalInterfaceZ(null);
		snakeValidator.firstLevelValidation(data);
		assertTrue(data.getNewSnaketest().getError().contains("Logical Interface Z is missing"));
		bert.setLogicalInterfaceZ("11/AB/Mask.0");

		bert.setPhysicalInterfaceA(null);
		snakeValidator.firstLevelValidation(data);
		assertTrue(data.getNewSnaketest().getError().contains("Physical Interface A is missing"));
		bert.setPhysicalInterfaceA("33/CD/Mask");

		bert.setPhysicalInterfaceZ(null);
		snakeValidator.firstLevelValidation(data);
		assertTrue(data.getNewSnaketest().getError().contains("Physical Interface Z is missing"));
		bert.setPhysicalInterfaceZ("33/CD/Mask.0");

		bert.setRouterA(null);
		snakeValidator.firstLevelValidation(data);
		assertTrue(data.getNewSnaketest().getError().contains("Router A is missing"));
		bert.setRouterA("10.0.0.1234");

		bert.setRouterZ(null);
		snakeValidator.firstLevelValidation(data);
		assertTrue(data.getNewSnaketest().getError().contains("Router Z is missing"));
		bert.setRouterZ("10.0.0.1234");
	}

	@Test
	public void duplicateInterfaceTest() {

		Data data = TestInputProvider.formDupIntRequest();

		List<BERT> berts = data.getNewSnaketest().getBERTS().getBERT();
		BERT bert = berts.get(0);
		BERT bert1 = berts.get(1);

		bert.setPhysicalInterfaceA("abc");
		bert1.setPhysicalInterfaceA("abc");
		snakeValidator.firstLevelValidation(data);
		assertTrue(data.getNewSnaketest().getError().contains("Duplicate Physical Interfaces were found "));
		bert.setPhysicalInterfaceA("abc");
		bert1.setPhysicalInterfaceA("xyz");

		bert.setLogicalInterfaceA("sample");
		bert1.setLogicalInterfaceA("sample");
		snakeValidator.firstLevelValidation(data);
		assertTrue(data.getNewSnaketest().getError().contains("Duplicate Logical Interfaces were found "));
		bert.setLogicalInterfaceA("sample");
		bert1.setLogicalInterfaceA("sample1");

		bert.setBERT("BERT-123");
		bert1.setBERT("BERT-123");
		snakeValidator.firstLevelValidation(data);
		assertTrue(data.getNewSnaketest().getError().contains("    Duplicate BERT ID's was found for " + " "
				+ bert1.getBERT() + " comparing with BERDID :" + bert.getBERT()));
		bert.setBERT("BERT-123");
		bert1.setBERT("BERT-987");

		bert.setCID("CID-123");
		bert1.setCID("CID-123");
		snakeValidator.firstLevelValidation(data);
		assertTrue(data.getNewSnaketest().getError().contains("    Duplicate CID's was found " + " " + bert1.getCID()
				+ " for BERDID :" + bert1.getBERT() + " comparing with BERDID :" + bert.getBERT()));
		bert.setCID("CID-123");
		bert1.setCID("CID-987");
	}

	@Test
	public void testforNoBerts() {
		Data data = new Data();
		NewSnaketest snkt = new NewSnaketest();
		BERTS berts = new BERTS();
		snkt.setUserid("user");
		snkt.setWfmId("9456");
		snkt.setWorktype("circuit");
		snkt.setBERTS(berts);
		data.setNewSnaketest(snkt);
		snakeValidator.firstLevelValidation(data);
		assertTrue(data.getNewSnaketest().getError().contains("No BERTS were found"));
	}*/
 private String xmlString="<Inventory>\r\n\r\n <Site id=\"ABCUDJ\" fmt=\"CLLI\" src=\"SKUD\">\r\n<Ownership>ABC</Ownership>\r\n\r\n<Node id=\"BTHYPAB\" fmt=\"TID\" src=\"NSDB\">\r\n<TypeID fmt=\"Model.NEType.NESubType.Hardware\">Tellabs7100.SBOADM.CH44.7100</TypeID>\r\n <Tid>BTHYPAB</Tid>\r\n</Node>\r\n</Site>\r\n</Inventory>";
}
