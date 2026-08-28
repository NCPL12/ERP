package com.ncpl.sales.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ncpl.sales.model.CompanyAssets;
import com.ncpl.sales.model.EmployeeMaster;
import com.ncpl.sales.model.ItemMaster;
import com.ncpl.sales.repository.CompanyAssetsRepo;
import com.ncpl.sales.repository.ItemMasterRepo;

@Service
public class CompanyAssetService {
	@Autowired
	CompanyAssetsRepo companyassetsRepo;
	@Autowired
	ItemMasterService itemService;
	@Autowired
	ItemMasterRepo itemRepo;
	@Autowired
	EmployeeService empService;
	
	public void saveCompanyAssets(CompanyAssets companyAssets) {
		
		Optional<CompanyAssets> companyAsset=companyAssetByModel(companyAssets.getModel());
		if(companyAsset.isPresent()) {
			updateCompanyAsset(companyAssets);
		}else {
			companyassetsRepo.save(companyAssets);
		}
		String itemId=companyAssets.getModel();
		Optional<ItemMaster> itemObj=itemService.getItemById(itemId);
		itemObj.get().setCompanyAssets(true);
		itemRepo.save(itemObj.get());
	}
	
	private void updateCompanyAsset(CompanyAssets companyAsset) {
		Optional<CompanyAssets> ca=companyAssetByModel(companyAsset.getModel());
		ca.get().setBrand(companyAsset.getBrand());
		ca.get().setCustodian(companyAsset.getCustodian());
		ca.get().setDate(companyAsset.getDate());
		ca.get().setFeatures(companyAsset.getFeatures());
		ca.get().setModel(companyAsset.getModel());
		ca.get().setReturnDate(companyAsset.getReturnDate());
		ca.get().setSite(companyAsset.getSite());
		ca.get().setSlNo(companyAsset.getSlNo());
		ca.get().setValue(companyAsset.getValue());
		ca.get().setWarranty(companyAsset.getWarranty());
		companyassetsRepo.save(ca.get());
	}

	public Optional<CompanyAssets> companyAssetByModel(String model){
		Optional<CompanyAssets> ca=companyassetsRepo.getCompanyAssetObjByModel(model);
		return ca;
	}

	public List<CompanyAssets> getAllCompanyAssetList() {
		List<CompanyAssets> companyAssetList=companyassetsRepo.findAll();
		for (CompanyAssets companyAssets : companyAssetList) {
			String itemId=companyAssets.getModel();
			Optional<ItemMaster> itemObj=itemService.getItemById(itemId);
			int empId=companyAssets.getCustodian();
			Optional<EmployeeMaster> empObj=empService.getEmployeeById(empId);
			companyAssets.set("modelName",itemObj.get().getModel());
			companyAssets.set("employee",empObj.get().getName());
		}
		return companyAssetList;
	}
}
