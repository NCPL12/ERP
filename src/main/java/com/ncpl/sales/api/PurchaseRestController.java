package com.ncpl.sales.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ncpl.sales.model.PurchaseOrder;

@RestController
public class PurchaseRestController {
	
	@PostMapping(value = "/poById")
	public ResponseEntity<?> getPOById(PurchaseOrder purchaseOrder){
		//System.out.println(">>>>>>>>>>>>>>:" +purchaseOrder.getPoNumber());
		return new ResponseEntity<PurchaseOrder>(HttpStatus.OK);
	}

}
