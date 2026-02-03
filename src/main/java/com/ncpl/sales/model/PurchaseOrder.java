package com.ncpl.sales.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.json.JSONArray;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.ncpl.sales.generator.PurchaseOrderIdGenerator;
import com.ncpl.sales.util.JSONObjectConverter;



@Entity
@Table(name="tbl_purchase_order")
/*@IdClass(PurchaseId.class)*/
public class PurchaseOrder extends TimeStampEntity implements Comparable<PurchaseOrder>{
		@Id
		@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "purchase_order_seq")
		@GenericGenerator(name = "purchase_order_seq", strategy = "com.ncpl.sales.generator.PurchaseOrderIdGenerator", parameters = {
				@Parameter(name = PurchaseOrderIdGenerator.NUMBER_FORMAT_PARAMETER, value = "%01d")})
		private String poNumber;
		private boolean archive;

		public PurchaseOrder() {
			
		}
		
       public PurchaseOrder(String poNo,List<PurchaseItem> items) {
			super();
			this.poNumber=poNo;
			this.items = items;
		}
		
		
		
		 @Column(columnDefinition = "TEXT")
		 @Convert(converter= JSONObjectConverter.class)
		 private JSONArray history;
		
		@OneToMany(cascade = CascadeType.ALL)
		@JoinColumn(name = "po_number")
		@JsonBackReference
		private List<PurchaseItem> items;
		
	/*
	 * @ManyToOne
	 * 
	 * @JsonManagedReference private SalesOrder salesOrder;
	 */
		
		@ManyToOne
		@JsonManagedReference
		private Party party;
		
		@Version
		private int version;
		
		@Transient
		private Double grandTotal;
		
		public Party getParty() {
			return party;
		}

		public void setParty(Party party) {
			this.party = party;
		}

	/*
	 * public SalesOrder getSalesOrder() { return salesOrder; }
	 * 
	 * public void setSalesOrder(SalesOrder salesOrder) { this.salesOrder =
	 * salesOrder; }
	 */
		

		public List<PurchaseItem> getItems() {
			return items;
		}

		public void setItems(List<PurchaseItem> items) {
			this.items = items;
		}

		public String getPoNumber() {
			return poNumber;
		}

		public void setPoNumber(String poNumber) {
			this.poNumber = poNumber;
		}

		public int getVersion() {
			return version;
		}

		public void setVersion(int version) {
			this.version = version;
		}

		public Double getGrandTotal() {
			return grandTotal;
		}

		public void setGrandTotal(Double grandTotal) {
			this.grandTotal = grandTotal;
		}
		
		public JSONArray getHistory() {
			return history;
		}

		public void setHistory(JSONArray history) {
			this.history = history;
		}
		
		public boolean isArchive() {
			return archive;
		}

		public void setArchive(boolean archive) {
			this.archive = archive;
		}
		

		@Override
		public String toString() {
			return "PurchaseOrder [poNumber=" + poNumber + ", history=" + history + ", items=" + items + ", party=" + party + ", version=" + version + ",archive="+archive+"]";
		}

		@Override
		public int compareTo(PurchaseOrder po) {
			String[] str1 = this.poNumber.split("-");
			List<String> list1 = Arrays.asList(str1);
			Collections.reverse(list1);
			String[] reversedArray1 = list1.toArray(str1);
			
			String[] str2 = po.poNumber.split("-");
			List<String> list2 = Arrays.asList(str2);
			Collections.reverse(list2);
			String[] reversedArray2 = list2.toArray(str2);
			
			
			return Integer.valueOf(reversedArray1[1]).compareTo(Integer.valueOf(reversedArray2[1]));
		}

		
}
