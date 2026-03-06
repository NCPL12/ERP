package com.ncpl.sales.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ncpl.sales.model.PurchaseOrder;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api(tags = "Purchase API", description = "Endpoints for Purchase Order operations")
@RestController
public class PurchaseRestController {

	@ApiOperation(value = "Get Purchase Order by ID", notes = "Returns a Purchase Order matching the given PO number")
	@PostMapping(value = "/poById")
	public ResponseEntity<?> getPOById(
			@ApiParam(value = "Purchase Order object containing the PO number", required = true)
			PurchaseOrder purchaseOrder) {
		return new ResponseEntity<PurchaseOrder>(HttpStatus.OK);
	}

}
