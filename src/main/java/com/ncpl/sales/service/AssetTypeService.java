package com.ncpl.sales.service;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ncpl.sales.model.AssetType;
import com.ncpl.sales.repository.AssetTypeRepo;

@Service
public class AssetTypeService {
	@Autowired
	AssetTypeRepo assetTypeRepo;

	public List<AssetType> getAssetTypeList() {
		List<AssetType> list = assetTypeRepo.findAll();
		Collections.sort(list);
		return list;
	}

	public AssetType saveAssetType(AssetType assetType) {
		if (!assetTypeRepo.findByNameIgnoreCase(assetType.getName().trim()).isEmpty()) {
			throw new IllegalArgumentException("Asset type already exists: " + assetType.getName());
		}
		assetType.setName(assetType.getName().trim());
		return assetTypeRepo.save(assetType);
	}
}
