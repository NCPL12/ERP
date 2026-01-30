package com.ncpl.sales.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Where;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "tbl_purchase_items")
public class PurchaseItem implements Comparable<PurchaseItem>{
		@Id
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		@Column(unique = true, nullable = false)
		private int purchase_item_id;
		@NotNull
		private float quantity;
		@NotNull
		private float amount;
		@NotNull
		private float unitPrice;
		@NotNull
		private String poDescription;
		
		private Date delivaryDate;
		private String lrNum;
		
		public String getPoDescription() {
			return poDescription;
		}

		public void setPoDescription(String poDescription) {
			this.poDescription = poDescription;
		}
		@Column(name="sales_item_id")
		private String description;

		@NotNull
		private String hsnCode;

		@NotNull
		private String modelNo;
		
		
		@ManyToOne
		@JsonManagedReference
		@JoinColumn(name="po_number")
		private PurchaseOrder purchaseOrder;

		public int getPurchase_item_id() {
			return purchase_item_id;
		}

		public void setPurchase_item_id(int purchase_item_id) {
			this.purchase_item_id = purchase_item_id;
		}


		public float getQuantity() {
			return quantity;
		}

		public void setQuantity(float quantity) {
			this.quantity = quantity;
		}

		public float getAmount() {
			return amount;
		}

		public void setAmount(float amount) {
			this.amount = amount;
		}
		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getHsnCode() {
			return hsnCode;
		}

		public void setHsnCode(String hsnCode) {
			this.hsnCode = hsnCode;
		}

		public String getModelNo() {
			return modelNo;
		}

		public void setModelNo(String modelNo) {
			this.modelNo = modelNo;
		}
		public float getUnitPrice() {
			return unitPrice;
		}

		public void setUnitPrice(float unitPrice) {
			this.unitPrice = unitPrice;
		}
		/* for jackson json */
		@Transient
		private Map<String, Object> others = new HashMap<String, Object>();

		@JsonAnyGetter
		public Map<String, Object> get() {
			return others;
		}
		
		public void set(Map<String, Object> data){
			others.putAll(data);
		}

		@JsonAnySetter
		public void set(String property, Object value) {
			others.put(property, value);
		}

		public Object get(String key) {
			Map<String, Object> others = this.get();
			return others.get(key);
		}


		@Override
		public String toString() {
			return "PurchaseItem [purchase_item_id=" + purchase_item_id + ",poDescription=" + poDescription + ", quantity=" + quantity + ", amount="
					+ amount + ", unitPrice=" + unitPrice + ", description=" + description + ", hsnCode=" + hsnCode
					+ ", modelNo=" + modelNo +"]";
		}

		public PurchaseOrder getPurchaseOrder() {
			return purchaseOrder;
		}

		public void setPurchaseOrder(PurchaseOrder purchaseOrder) {
			this.purchaseOrder = purchaseOrder;
		}

		public Date getDelivaryDate() {
			return delivaryDate;
		}

		public void setDelivaryDate(Date delivaryDate) {
			this.delivaryDate = delivaryDate;
		}

		public String getLrNum() {
			return lrNum;
		}

		public void setLrNum(String lrNum) {
			this.lrNum = lrNum;
		}

		@Override
		public int compareTo(PurchaseItem poItem) {
			if (this.delivaryDate == null || poItem.delivaryDate == null)
			      return 0;
			return this.delivaryDate.compareTo(poItem.delivaryDate);
		}


		
		
}
