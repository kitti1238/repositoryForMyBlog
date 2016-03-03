package com.org.coop.customer.ws;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.org.coop.canonical.beans.UIModel;
import com.org.coop.retail.data.entities.MaterialMaster;
import com.org.coop.retail.data.repositories.MaterialMasterRepository;

@RestController
@RequestMapping("/rest")
public class MasterDataSetupWSImpl {
	
	private static final Logger log = Logger.getLogger(MasterDataSetupWSImpl.class); 
	
	
	@RequestMapping(value = "/getBranch", method = RequestMethod.GET, headers="Accept=application/json",produces="application/json")
	public UIModel getBranch(@RequestParam(value = "branchId",required = true,defaultValue = "0") Integer branchId) {
		UIModel uiModel = new UIModel();
		try {
//			uiModel = branchSetupServiceImpl.getBranch(branchId);
			deleteMaterials();
			addMaterial();
//			addAnotherMaterial();
			MaterialMaster mm = getMaterial();
		} catch (Exception e) {
			log.error("Error Retrieving branch by branch Id", e);
			uiModel.setErrorMsg("Error Retrieving branch by branch Id: " + branchId);
		}
		return uiModel;
	}
	
	
	@Autowired
	private MaterialMasterRepository materialMasterRepository;
	
	public void deleteMaterials() {
		List<MaterialMaster> materials = materialMasterRepository.findAll();
		materialMasterRepository.delete(materials);
	}
	
	public void addMaterial() {
		MaterialMaster mm = new MaterialMaster();
		mm.setMaterialId(1L);
		mm.setName("Maxx Engine Oil");
		materialMasterRepository.save(mm);
	}
	
	public void addAnotherMaterial() {
		MaterialMaster mm = new MaterialMaster();
		mm.setMaterialId(2L);
		mm.setName("Castrol Engine Oil");
		materialMasterRepository.save(mm);
	}
	
	public MaterialMaster getMaterial() {
		MaterialMaster mm = materialMasterRepository.findByMaterialId(1L);
		return mm;
	}
}
