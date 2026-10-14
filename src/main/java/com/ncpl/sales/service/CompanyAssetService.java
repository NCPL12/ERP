package com.ncpl.sales.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ncpl.sales.model.CompanyAssets;
import com.ncpl.sales.model.ItemMaster;
import com.ncpl.sales.model.LaptopAssignment;
import com.ncpl.sales.repository.CompanyAssetsRepo;
import com.ncpl.sales.repository.EmployeeRepo;
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
	EmployeeRepo employeeRepo;

	/**
	 * Same shape as SalesService.savesales(): the whole parent + its
	 * assignments[N] rows come in on one @ModelAttribute-bound object and are
	 * saved together via cascade, instead of a separate per-row endpoint.
	 */
	public void saveCompanyAssets(CompanyAssets companyAssets) {
		if (companyAssets.getAssignments() == null) {
			companyAssets.setAssignments(new ArrayList<>());
		}
		resolveAssignmentEmployees(companyAssets);

		if (companyAssets.getId() == 0) {
			for (LaptopAssignment a : companyAssets.getAssignments()) {
				a.setCompanyAsset(companyAssets);
			}
			// sl_no is NOT NULL in the DB; placeholder satisfies the insert until the
			// real id-based value can be set below.
			companyAssets.setSlNo("");
			companyassetsRepo.save(companyAssets);
			// Sl.No is auto-generated from the DB-assigned id, not user-entered.
			companyAssets.setSlNo(String.valueOf(companyAssets.getId()));
			companyassetsRepo.save(companyAssets);
		} else {
			CompanyAssets existing = companyassetsRepo.findById(companyAssets.getId())
					.orElseThrow(() -> new IllegalArgumentException("No such company asset: " + companyAssets.getId()));

			// preserve server-managed fields the form never submits
			companyAssets.setCreated(existing.getCreated());
			companyAssets.setCreatedBy(existing.getCreatedBy());
			companyAssets.setSlNo(existing.getSlNo());

			Map<Integer, Date> oldCreated = new HashMap<>();
			Map<Integer, String> oldCreatedBy = new HashMap<>();
			for (LaptopAssignment old : existing.getAssignments()) {
				oldCreated.put(old.getId(), old.getCreated());
				oldCreatedBy.put(old.getId(), old.getCreatedBy());
			}

			List<LaptopAssignment> submitted = companyAssets.getAssignments();
			Set<Integer> submittedIds = new HashSet<>();
			for (LaptopAssignment a : submitted) {
				a.setCompanyAsset(companyAssets);
				if (a.getId() != 0) {
					submittedIds.add(a.getId());
					a.setCreated(oldCreated.get(a.getId()));
					a.setCreatedBy(oldCreatedBy.get(a.getId()));
				}
			}
			// keep any existing rows this submission didn't include (stale/partial edit guard)
			for (LaptopAssignment old : existing.getAssignments()) {
				if (!submittedIds.contains(old.getId())) {
					old.setCompanyAsset(companyAssets);
					submitted.add(old);
				}
			}

			companyassetsRepo.save(companyAssets);
		}

		getItemByModel(companyAssets.getModel()).ifPresent(item -> {
			item.setCompanyAssets(true);
			itemRepo.save(item);
		});
	}

	private void resolveAssignmentEmployees(CompanyAssets companyAssets) {
		for (LaptopAssignment a : companyAssets.getAssignments()) {
			String employeeId = a.getEmployeeId();
			if (employeeId == null || employeeId.trim().isEmpty()) {
				continue;
			}
			try {
				employeeRepo.findById(Integer.valueOf(employeeId.trim())).ifPresent(a::setEmployee);
			} catch (NumberFormatException ignored) {
				// row submitted without a valid employee selection — leave employee unset
			}
		}
	}

	public void deleteCompanyAsset(int id) {
		companyassetsRepo.deleteById(id);
	}

	/**
	 * Laptop rows are not tied to a delivery challan item, so model may not
	 * match any ItemMaster (or may be blank) — findById(null) throws.
	 */
	private Optional<ItemMaster> getItemByModel(String model) {
		if (model == null || model.trim().isEmpty()) {
			return Optional.empty();
		}
		return itemService.getItemById(model);
	}

	public List<CompanyAssets> getAllCompanyAssetList() {
		List<CompanyAssets> companyAssetList=companyassetsRepo.findAll();
		companyAssetList.forEach(this::enrichForDisplay);
		return companyAssetList;
	}

	public Optional<CompanyAssets> getCompanyAssetById(int id) {
		Optional<CompanyAssets> asset = companyassetsRepo.findById(id);
		asset.ifPresent(this::enrichForDisplay);
		return asset;
	}

	private void enrichForDisplay(CompanyAssets companyAssets) {
		Optional<ItemMaster> itemObj = getItemByModel(companyAssets.getModel());
		companyAssets.set("modelName", itemObj.map(ItemMaster::getModel).orElse(companyAssets.getModel()));
	}
}
