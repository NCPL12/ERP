package com.ncpl.sales.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ncpl.sales.model.AssetType;
import com.ncpl.sales.service.AssetTypeService;

@RestController
@RequestMapping("/api/asset_type")
public class AssetTypeRestController {

	@Autowired
	private AssetTypeService assetTypeService;

	@GetMapping("/list")
	public List<AssetType> list() {
		return assetTypeService.getAssetTypeList();
	}

	@PostMapping("/add")
	public ResponseEntity<?> add(@RequestParam("name") String name) {
		if (name == null || name.trim().isEmpty()) {
			return ResponseEntity.badRequest().body("Asset type name is required");
		}
		try {
			AssetType entity = new AssetType();
			entity.setName(name.trim());
			AssetType saved = assetTypeService.saveAssetType(entity);
			return ResponseEntity.ok(saved);
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
		} catch (Exception ex) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
		}
	}
}
