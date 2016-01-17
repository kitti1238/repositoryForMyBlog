package com.org.coop.admin.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.org.coop.bs.mapper.BranchMappingImpl;
import com.org.coop.bs.mapper.CountryStateDistMappingImpl;
import com.org.coop.bs.mapper.ModuleMappingImpl;
import com.org.coop.canonical.beans.BranchBean;
import com.org.coop.canonical.beans.UIModel;
import com.org.coop.canonical.master.beans.CountryMasterBean;
import com.org.coop.canonical.master.beans.DistrictMasterBean;
import com.org.coop.canonical.master.beans.MasterDataBean;
import com.org.coop.canonical.master.beans.ModuleMasterBean;
import com.org.coop.canonical.master.beans.StateMasterBean;
import com.org.coop.society.data.admin.entities.CountryMaster;
import com.org.coop.society.data.admin.entities.ModuleMaster;
import com.org.coop.society.data.admin.repositories.CountryMasterRepository;
import com.org.coop.society.data.admin.repositories.ModuleMasterRepository;

@Service
public class MasterDataSetupServiceImpl {

	private static final Logger log = Logger.getLogger(MasterDataSetupServiceImpl.class); 
	
	@Autowired
	private CountryMasterRepository countryMasterRepository;
	
	@Autowired
	private ModuleMasterRepository moduleMasterRepository;
	
	@Autowired
	private CountryStateDistMappingImpl countryStateDistMap;
	
	@Autowired
	private ModuleMappingImpl moduleMap;
	
	@Transactional(value="adminTransactionManager")
	public MasterDataBean saveCountryStateDist(MasterDataBean masterDataBean) {
		
		if(masterDataBean == null || masterDataBean.getCountries() == null) {
			masterDataBean.setErrorMsg("Country details not passed correctly");
		}
		
		Set<CountryMasterBean> countries = masterDataBean.getCountries();
		
		for(CountryMasterBean countryBean: countries) {
			CountryMaster country = countryMasterRepository.findByCountryId(countryBean.getCountryId());
			if(country == null) {
				country = new CountryMaster();
			}
			countryStateDistMap.mapBean(countryBean, country);
			countryMasterRepository.saveAndFlush(country);
			if(log.isDebugEnabled()) {
				log.debug("Country/State/District created/updated for countryId: " + countryBean.getCountryId());
			}
		}
		masterDataBean = getCountryStateDist(((CountryMasterBean)masterDataBean.getCountries().toArray()[0]).getCountryCode(), "", "");
		return masterDataBean;
	}
	
	@Transactional(value="adminTransactionManager")
	public MasterDataBean getCountryStateDist(String countryCode,String stateCode,String distCode) {
		MasterDataBean masterData = new MasterDataBean();
		// Check if the branch already exists
		CountryMaster country = countryMasterRepository.findByCountryCode(countryCode);
		if(country == null) {
			masterData.setErrorMsg("Country does not exist for Country Code: " + countryCode);
			return masterData;
		}
		CountryMasterBean countryMaster = new CountryMasterBean();
		Set<CountryMasterBean> countrySet = new HashSet<CountryMasterBean>();
		countrySet.add(countryMaster);
		countryStateDistMap.mapBean(country, countryMaster);
		masterData.setCountries(countrySet);
		
		
		// Remove other states from the master data
		if(StringUtils.isNotEmpty(stateCode)) {
			for(CountryMasterBean countryBean : masterData.getCountries()) {
				if(countryBean.getStates() != null) {
					Iterator<StateMasterBean> it = countryBean.getStates().iterator();
					while(it.hasNext()) {
						if(!stateCode.equals(it.next().getStateCode())) {
							it.remove();
						}
					}
				}
			}
			
			// Remove other districts from the master data
			if(StringUtils.isNotEmpty(distCode)) {
				for(CountryMasterBean countryBean : masterData.getCountries()) {
					for(StateMasterBean statBean : countryBean.getStates()) {
						if(statBean.getDistricts() != null) {
							Iterator<DistrictMasterBean> it = statBean.getDistricts().iterator();
							while(it.hasNext()) {
								if(!distCode.equals(it.next().getDistrictCode())) {
									it.remove();
								}
							}
						}
					}
				}
			}
		}
		log.debug("Branch details has been retrieved from database for branchId: " + countryCode);
		return masterData;
	}
	
	@Transactional(value="adminTransactionManager")
	public MasterDataBean getModuleRulesAndPermissions(String moduleName) {
		
		MasterDataBean masterData = new MasterDataBean();
		List<ModuleMaster> moduleList = new ArrayList<ModuleMaster>();
		if(StringUtils.isNotBlank(moduleName)) {
			ModuleMaster module = moduleMasterRepository.findByModuleName(moduleName);
			
			if(module == null) {
				masterData.setErrorMsg("Module does not exist for Module Name: " + moduleName);
				return masterData;
			}
			moduleList.add(module);
		} else {
			moduleList = moduleMasterRepository.findAll();
			if(moduleList == null || moduleList.size() == 0) {
				masterData.setErrorMsg("No modules available in the database");
				return masterData;
			}
		}
		Set<ModuleMasterBean> moduleSet = new HashSet<ModuleMasterBean>();
		for(ModuleMaster module : moduleList) {
			ModuleMasterBean moduleMasterBean = new ModuleMasterBean();
			moduleSet.add(moduleMasterBean);
			moduleMap.mapBean(module, moduleMasterBean);
		}
		
		masterData.setModules(moduleSet);
		
		return masterData;
	}
}