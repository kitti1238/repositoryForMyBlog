package com.org.coop.customer.ws;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.org.coop.canonical.beans.UIModel;
import com.org.coop.retail.service.FinancialYearCloseServiceImpl;

@RestController
@RequestMapping("/rest")
public class FinancialYearCloseWSImpl {

	private static final Logger log = Logger.getLogger(FinancialYearCloseWSImpl.class); 
	
	@Autowired
	private FinancialYearCloseServiceImpl financialYearCloseServiceImpl;
	
	@RequestMapping(value = "/closeFinancialYearData", method = RequestMethod.POST, headers="Accept=application/json",produces="application/json")
	public UIModel closeFinancialYearData(@RequestBody UIModel uiModel) {
		try {
			uiModel = financialYearCloseServiceImpl.closeFinancialYearData(uiModel);
		} catch (Exception e) {
			log.error("Error while saving financial year data", e);
			uiModel.setErrorMsg("Error while saving financial year data for branch Id: " + uiModel.getBranchBean().getBranchId());
		}
		return uiModel;
	}
}
