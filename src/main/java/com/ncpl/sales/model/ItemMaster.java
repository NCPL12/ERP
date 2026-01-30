package com.ncpl.sales.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.ncpl.sales.generator.ItemMasterIdGenerator;

@Entity
@Table(name = "tbl_item_master")
@Audited(targetAuditMode=RelationTargetAuditMode.NOT_AUDITED)
public class ItemMaster extends TimeStampEntity implements Comparable<ItemMaster> {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "item_master_seq")
	@GenericGenerator(name = "item_master_seq", strategy = "com.ncpl.sales.generator.ItemMasterIdGenerator", parameters = {
			@Parameter(name = ItemMasterIdGenerator.NUMBER_FORMAT_PARAMETER, value = "%01d")})
	private String id;
	private String itemName;
	
	private String model;
	private String hsnCode;
	private double sellPrice;
	private double minSellPrice;
	private int gst;
	private String location;
	private String make;
	private boolean toolTracker;
	private boolean companyAssets;
	
	//private boolean nonBillable;
	
	@Transient
	private Long units;
	
	@Transient
	private double prefferedCost;
	
	@OneToOne(fetch= FetchType.EAGER)
	@JoinColumn(name = "units_id")
	private Units item_units;
	
	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "item_id")
	@JsonBackReference
	private List<Stock> stock;
	
	public List<Stock> getStock() {
		return stock;
	}

	public void setStock(List<Stock> stock) {
		this.stock = stock;
	}

	public Units getItem_units() {
		return item_units;
	}

	public Long getUnits() {
		return units;
	}

	public void setUnits(Long units) {
		this.units = units;
	}

	public void setItem_units(Units item_units) {
		this.item_units = item_units;
	}

	public ItemMaster() {
		
	}
	
	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getHsnCode() {
		return hsnCode;
	}

	public void setHsnCode(String hsnCode) {
		this.hsnCode = hsnCode;
	}


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}
	
	public double getSellPrice() {
		return sellPrice;
	}

	public void setSellPrice(double sellPrice) {
		this.sellPrice = sellPrice;
	}

	public double getMinSellPrice() {
		return minSellPrice;
	}

	public void setMinSellPrice(double minSellPrice) {
		this.minSellPrice = minSellPrice;
	}

	
	public int getGst() {
		return gst;
	}

	public void setGst(int gst) {
		this.gst = gst;
	}

	
	public double getPrefferedCost() {
		return prefferedCost;
	}

	public void setPrefferedCost(double prefferedCost) {
		this.prefferedCost = prefferedCost;
	}

	@Override
	public String toString() {
		return "ItemMaster [id=" + id + ", itemName=" + itemName + ", model=" + model
				+ ", hsnCode=" + hsnCode + ", units=" + units + "]";
	}

	@Override
	public int compareTo(ItemMaster itemMaster) {
		return this.model.compareToIgnoreCase(itemMaster.model);
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getMake() {
		return make;
	}

	public void setMake(String make) {
		this.make = make;
	}

	public boolean isToolTracker() {
		return toolTracker;
	}

	public void setToolTracker(boolean toolTracker) {
		this.toolTracker = toolTracker;
	}

	public boolean isCompanyAssets() {
		return companyAssets;
	}

	public void setCompanyAssets(boolean companyAssets) {
		this.companyAssets = companyAssets;
	}

	/*public boolean isNonBillable() {
		return nonBillable;
	}

	public void setNonBillable(boolean nonBillable) {
		this.nonBillable = nonBillable;
	}*/

	


}
