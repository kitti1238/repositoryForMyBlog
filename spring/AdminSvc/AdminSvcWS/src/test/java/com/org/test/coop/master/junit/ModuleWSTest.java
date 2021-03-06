package com.org.test.coop.master.junit;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.org.coop.admin.service.BranchSetupServiceImpl;
import com.org.coop.admin.service.MasterDataSetupServiceImpl;
import com.org.coop.bs.config.DozerConfig;
import com.org.coop.canonical.beans.AddressBean;
import com.org.coop.canonical.beans.BranchBean;
import com.org.coop.canonical.beans.UIModel;
import com.org.coop.canonical.master.beans.MasterDataBean;
import com.org.coop.canonical.master.beans.ModuleMasterBean;
import com.org.coop.canonical.master.beans.ModulePermissionMasterBean;
import com.org.coop.canonical.master.beans.RuleMasterBean;
import com.org.coop.canonical.master.beans.RuleMasterValuesBean;
import com.org.coop.society.data.admin.entities.BranchMaster;
import com.org.coop.society.data.admin.repositories.BranchMasterRepository;
import com.org.coop.society.data.transaction.config.DataAppConfig;
import com.org.test.coop.junit.JunitTestUtil;
import com.org.test.coop.society.data.transaction.config.TestDataAppConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@ComponentScan(basePackages = "com.org.test.coop"/*, excludeFilters = { @Filter(type = FilterType.ANNOTATION, value = DataAppConfig.class) }*/)
@EnableAutoConfiguration(exclude = { DataAppConfig.class})
@ContextHierarchy({
	  @ContextConfiguration(classes={TestDataAppConfig.class, DozerConfig.class})
})
@WebAppConfiguration
public class ModuleWSTest {
	private static final Logger logger = Logger.getLogger(ModuleWSTest.class);
	
	private MockMvc mockMvc;
	@Autowired
	private WebApplicationContext wac;
	
	private MasterDataBean createModuleBean;
	private MasterDataBean createAnotherModuleBean;
	private MasterDataBean updateModuleBean;
	private MasterDataBean moduleRulePermBean;
	private MasterDataBean createModuleRuleValueBean;
	
	
	private String createModuleJson = null;
	private String createAnotherModuleJson = null;
	private String updateModuleJson = null;
	private String moduleRulePermJson = null;
	private String createModuleRuleValueJson = null;
	private String addAnotherModuleRule = null;
	private String addAnotherModuleRuleValue = null;
	private String addAnotherModulePermission1 = null;
	private String addAnotherModulePermission2 = null;
	private String addAnotherModulePermission3 = null;
	
	private ObjectMapper om = null;
	
	@Autowired
	private MasterDataSetupServiceImpl masterDataSetupServiceImpl;
	
	@Before
	public void runBefore() {
		try {
			this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

			DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			om = new ObjectMapper();
			om.setSerializationInclusion(Include.NON_NULL);
			om.setDateFormat(df);
			createModuleJson = JunitTestUtil.getFileContent("inputJson/master/module/createModule.json");
			createModuleBean = om.readValue(createModuleJson, MasterDataBean.class);
			
			
			updateModuleJson = JunitTestUtil.getFileContent("inputJson/master/module/updateModule.json");
			updateModuleBean = om.readValue(updateModuleJson, MasterDataBean.class);
			
			moduleRulePermJson = JunitTestUtil.getFileContent("inputJson/master/module/createPermissionAndRule.json");
			moduleRulePermBean = om.readValue(moduleRulePermJson, MasterDataBean.class);
			
			createAnotherModuleJson = JunitTestUtil.getFileContent("inputJson/master/module/createAnotherModule.json");
			createAnotherModuleBean = om.readValue(createAnotherModuleJson, MasterDataBean.class);
			
			createModuleRuleValueJson = JunitTestUtil.getFileContent("inputJson/master/module/createModuleRuleValue.json");
			createModuleRuleValueBean = om.readValue(createModuleRuleValueJson, MasterDataBean.class);
			
			addAnotherModuleRule = JunitTestUtil.getFileContent("inputJson/master/module/addAnotherModuleRule.json");
			addAnotherModuleRuleValue = JunitTestUtil.getFileContent("inputJson/master/module/addAnotherModuleRuleValue.json");
			
			addAnotherModulePermission1 = JunitTestUtil.getFileContent("inputJson/master/module/addAnotherModulePermission1.json");
			addAnotherModulePermission2 = JunitTestUtil.getFileContent("inputJson/master/module/addAnotherModulePermission2.json");
			addAnotherModulePermission3 = JunitTestUtil.getFileContent("inputJson/master/module/addAnotherModulePermission3.json");
		} catch (Exception e) {
			logger.error("Error while initializing: ", e);
		}
	}
	@Test
	public void testModule() {
		createModule();
		createDuplicateModule();
		createAnotherModule();
		updateModule();
		getValidModule();
		getNonExistanceModule();
		createModuleRuleAndPermission();
		getAllModules();
		createModuleRuleValues();
		addAnotherModuleRule();
		addAnotherModuleRuleValue();
		addAnotherModulePermission1();
		addAnotherModulePermission2();
		addAnotherModulePermission3();
	}

	private void createModule() {
		try {
			MasterDataBean moduleBean = masterDataSetupServiceImpl.saveModuleRulesAndPermissions(createModuleBean);
			
			String srcJson = om.writeValueAsString(moduleBean);
			moduleBean = masterDataSetupServiceImpl.getModuleRulesAndPermissions(((ModuleMasterBean)createModuleBean.getModules().toArray()[0]).getModuleName());
			String destJson = om.writeValueAsString(moduleBean);
			
			assertEquals(srcJson, destJson);
			
		} catch (Exception e) {
			logger.error("Error while creating module/duplicate module: ", e);
			Assert.fail("Error while creating module/duplicate module:");
		}
	}
	
	private void createAnotherModule() {
		try {
			MasterDataBean moduleBean = masterDataSetupServiceImpl.saveModuleRulesAndPermissions(createAnotherModuleBean);
			
			String srcJson = om.writeValueAsString(moduleBean);
			moduleBean = masterDataSetupServiceImpl.getModuleRulesAndPermissions(((ModuleMasterBean)createAnotherModuleBean.getModules().toArray()[0]).getModuleName());
			String destJson = om.writeValueAsString(moduleBean);
			
			assertEquals(srcJson, destJson);
			
		} catch (Exception e) {
			logger.error("Error while creating module/duplicate module: ", e);
			Assert.fail("Error while creating module/duplicate module:");
		}
	}
	
	private void updateModule() {
		try {
			MasterDataBean moduleBean = masterDataSetupServiceImpl.saveModuleRulesAndPermissions(updateModuleBean);

			String srcJson = om.writeValueAsString(moduleBean);
			moduleBean = masterDataSetupServiceImpl.getModuleRulesAndPermissions(((ModuleMasterBean)updateModuleBean.getModules().toArray()[0]).getModuleName());
			String destJson = om.writeValueAsString(moduleBean);
			
			assertEquals(srcJson, destJson);
		} catch (Exception e) {
			logger.error("Error while creating module: ", e);
			Assert.fail("Error while creating module:");
		}
	}
	
	private void createDuplicateModule() {
		try {
			// Create duplicate module 
			MasterDataBean moduleBean = masterDataSetupServiceImpl.saveModuleRulesAndPermissions(createModuleBean);
			assertNotEquals(moduleBean.getErrorMsg(), null);
		} catch (Exception e) {
			logger.error("Error while creating module: ", e);
			Assert.fail("Error while creating module:");
		}
	}
	
	private void getValidModule() {
		try {
			MasterDataBean moduleBean = masterDataSetupServiceImpl.getModuleRulesAndPermissions("RD");
			assertEquals(moduleBean.getErrorMsg(), null);
		} catch (Exception e) {
			logger.error("Error while validating module: ", e);
			Assert.fail("Error while validating module:");
		}
	}
	
	private void getNonExistanceModule() {
		try {
			MasterDataBean moduleBean = masterDataSetupServiceImpl.getModuleRulesAndPermissions("RD1");
			assertNotEquals(moduleBean.getErrorMsg(), null);
		} catch (Exception e) {
			logger.error("Error while validating non existance module: ", e);
			Assert.fail("Error while validating non existance module:");
		}
	}
	
	private void createModuleRuleAndPermission() {
		try {
			MasterDataBean moduleBean = masterDataSetupServiceImpl.saveModuleRulesAndPermissions(moduleRulePermBean);
			
			String srcJson = om.writeValueAsString(moduleBean);
			moduleBean = masterDataSetupServiceImpl.getModuleRulesAndPermissions(((ModuleMasterBean)moduleRulePermBean.getModules().toArray()[0]).getModuleName());
			String destJson = om.writeValueAsString(moduleBean);
			
			assertEquals(srcJson, destJson);
		} catch (Exception e) {
			logger.error("Error while creating module rules and permission: ", e);
			Assert.fail("Error while creating module rules and permission:");
		}
	}
	
	private void addAnotherModuleRule() {
		try {
			MvcResult result = this.mockMvc.perform(post("/rest/saveModuleRulesAndPermissions")
					 .contentType("application/json").header("Authorization", "Basic " + Base64.getEncoder().encodeToString("ashish:ashish".getBytes()))
					 .content(addAnotherModuleRule)
					).andExpect(status().isOk())
					.andExpect(content().contentType("application/json"))
					.andReturn();
				
			MasterDataBean masterData = getMasterDataBean(result);
			
			RuleMasterBean rule = (RuleMasterBean)((ModuleMasterBean)masterData.getModules().toArray()[0]).getModuleRules().toArray()[1];
			
			assertEquals(masterData.getErrorMsg(), null);
//			assertEquals(rule.getRuleName(), "NUMBER_OF_TIMES_OTP_TO_BE_RESEND");
//			assertEquals(rule.getRuleDescription(), "Number of times OTP to be resend");
			assertEquals(2, ((ModuleMasterBean)masterData.getModules().toArray()[0]).getModuleRules().size());
		} catch (Exception e) {
			logger.error("Error while creating module rules and permission: ", e);
			Assert.fail("Error while creating module rules and permission:");
		}
	}
	
	private void addAnotherModuleRuleValue() {
		try {
			MvcResult result = this.mockMvc.perform(post("/rest/saveModuleRulesAndPermissions")
					 .contentType("application/json").header("Authorization", "Basic " + Base64.getEncoder().encodeToString("ashish:ashish".getBytes()))
					 .content(addAnotherModuleRuleValue)
					).andExpect(status().isOk())
					.andExpect(content().contentType("application/json"))
					.andReturn();
				
			MasterDataBean masterData = getMasterDataBean(result, "outputJson/master/module/addAnotherModuleRuleValue.json");
			RuleMasterValuesBean ruleVal = (RuleMasterValuesBean)((RuleMasterBean)((ModuleMasterBean)masterData.getModules().toArray()[0]).getModuleRules().toArray()[0]).getRuleMasterValues().toArray()[0];
			
			assertEquals(masterData.getErrorMsg(), null);
			assertEquals(ruleVal.getRuleValue(), "5");
			
		} catch (Exception e) {
			logger.error("Error while creating module rules and permission: ", e);
			Assert.fail("Error while creating module rules and permission:");
		}
	}
	
	private MasterDataBean getMasterDataBean(MvcResult result)
			throws UnsupportedEncodingException, IOException,
			JsonParseException, JsonMappingException {
		String json = result.getResponse().getContentAsString();
		MasterDataBean masterData = om.readValue(json, MasterDataBean.class);
		return masterData;
	}
	
	private MasterDataBean getMasterDataBean(MvcResult result, String path)
			throws UnsupportedEncodingException, IOException,
			JsonParseException, JsonMappingException {
		MasterDataBean masterDataBean = getMasterDataBean(result);
		JunitTestUtil.writeJSONToFile(masterDataBean, path);
		return masterDataBean;
	}
	
	private void getAllModules() {
		try {
			MasterDataBean moduleBean = masterDataSetupServiceImpl.getModuleRulesAndPermissions("");
			assertEquals(moduleBean.getModules().size(), 2);
		} catch (Exception e) {
			logger.error("Error while Retriving module: ", e);
			Assert.fail("Error while Retriving module:");
		}
	}
	
	private void createModuleRuleValues() {
		try {
			MasterDataBean moduleBean = masterDataSetupServiceImpl.saveModuleRulesAndPermissions(createModuleRuleValueBean);
			RuleMasterValuesBean ruleVal = ((RuleMasterValuesBean)((RuleMasterBean)((ModuleMasterBean)moduleBean.getModules().toArray()[0]).getModuleRules().toArray()[0]).getRuleMasterValues().toArray()[0]);
			assertEquals(ruleVal.getRuleValue(), "5");
			assertEquals(ruleVal.getRuleName(), "LOCK_AFTER_NO_OF_ATTEMPTS");
		} catch (Exception e) {
			logger.error("Error while Retriving module: ", e);
			Assert.fail("Error while Retriving module:");
		}
	}
	
	private void addAnotherModulePermission1() {
		try {
			MvcResult result = this.mockMvc.perform(post("/rest/saveModuleRulesAndPermissions")
				 .contentType("application/json").header("Authorization", "Basic " + Base64.getEncoder().encodeToString("ashish:ashish".getBytes()))
				 .content(addAnotherModulePermission1)
				).andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andReturn();
			
			MasterDataBean masterData = getMasterDataBean(result, "outputJson/master/module/addAnotherModulePermission1.json");
			assertEquals(null, masterData.getErrorMsg());
			
			for(ModuleMasterBean module: masterData.getModules()) {
				if(module.getModuleId() == 2) {
					for(ModulePermissionMasterBean perm : module.getModulePermissions()) {
						if("READ".equals(perm.getPermission())) {
							assertEquals("READ", perm.getPermission());
						}
					}
				}
			}
		} catch(Exception e) {
			logger.error("Error while adding module permission", e);
		}
	}
	
	private void addAnotherModulePermission2() {
		try {
			MvcResult result = this.mockMvc.perform(post("/rest/saveModuleRulesAndPermissions")
				 .contentType("application/json").header("Authorization", "Basic " + Base64.getEncoder().encodeToString("ashish:ashish".getBytes()))
				 .content(addAnotherModulePermission2)
				).andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andReturn();
			
			MasterDataBean masterData = getMasterDataBean(result, "outputJson/master/module/addAnotherModulePermission2.json");
			assertEquals(null, masterData.getErrorMsg());
			
			for(ModuleMasterBean module: masterData.getModules()) {
				if(module.getModuleId() == 2) {
					for(ModulePermissionMasterBean perm : module.getModulePermissions()) {
						if("UPDATE".equals(perm.getPermission())) {
							assertEquals("UPDATE", perm.getPermission());
						}
					}
				}
			}
		} catch(Exception e) {
			logger.error("Error while adding module permission", e);
		}
	}
	
	private void addAnotherModulePermission3() {
		try {
			MvcResult result = this.mockMvc.perform(post("/rest/saveModuleRulesAndPermissions")
				 .contentType("application/json").header("Authorization", "Basic " + Base64.getEncoder().encodeToString("ashish:ashish".getBytes()))
				 .content(addAnotherModulePermission3)
				).andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andReturn();
			
			MasterDataBean masterData = getMasterDataBean(result, "outputJson/master/module/addAnotherModulePermission3.json");
			assertEquals(null, masterData.getErrorMsg());
			
			for(ModuleMasterBean module: masterData.getModules()) {
				if(module.getModuleId() == 2) {
					for(ModulePermissionMasterBean perm : module.getModulePermissions()) {
						if("DELETE".equals(perm.getPermission())) {
							assertEquals("DELETE", perm.getPermission());
						}
					}
				}
			}
		} catch(Exception e) {
			logger.error("Error while adding module permission", e);
		}
	}
	
}
