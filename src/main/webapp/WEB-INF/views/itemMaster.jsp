<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>

	<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
		<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
			<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
				<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>


					<c:url var="ROOT" value="/"></c:url>
					<c:url var="RESOURCES" value="/resources/"></c:url>
					<!DOCTYPE html>
					<html>

					<head>
						<meta charset="ISO-8859-1">
						<title>
							<tiles:insertAttribute name="title" />
						</title>
						<tiles:insertAttribute name="header-resources" />


						<!-- <link rel="stylesheet" href="resources/css/salesOrder.css"> -->
						<link rel="stylesheet" href="<c:url value=" /resources/css/salesOrder.css" />">
						<link rel="stylesheet" href="<c:url value=" /resources/css/item-master.css" />">
						<!--  <script src="<c:url value="/resources/js/salesOrder.js" />"></script> -->
						<script src="<c:url value=" /resources/js/itemMaster.js" />"></script>
						<script src="${RESOURCES}js/common.js"></script>


						<link rel="stylesheet" href="//code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css">
						<script src="https://code.jquery.com/ui/1.12.1/jquery-ui.js"></script>
						<script type="text/javascript">
							var itemList = ${ itemList };
							var customerPartyList = ${ customerPartyList };
							var supplierPartyList = ${ supplierPartyList };
							var allSupplierslist = ${ allSupplierslist };
							var allStocksList = ${ allStocksList };
							var makeList = ${ makeList };
							var role = ${ role };
							var user = ${ user };
						</script>
						<style type="text/css">
							.custom-box-header {
								color: color: #343a40 !important;
								padding: 4px 4px 4px 4px !important;
								background: #4e595f !important;
								background-color: rgba(45, 147, 203, 0.84) !important;
								margin: 0;
							}

							.header-font {
								font-size: 21px !important;
							}

							#units+.select2-container--default .select2-selection--single .select2-selection__rendered {
								margin-top: -5px !important;
							}

							#units+.select2-container--default .select2-selection--single {
								border: 1px solid #ced4da !important;
								height: 32px !important;
								padding-top: 10px !important;
							}

							#clientName+.select2-container--default .select2-selection--single {
								border: 1px solid #ced4da !important;
								height: 32px !important;
								padding-top: 10px !important;
							}

							#supplierName+.select2-container--default .select2-selection--single {
								border: 1px solid #ced4da !important;
								height: 32px !important;
								padding-top: 10px !important;
							}

							.footerDivinType {
								width: 180px;
								margin-left: auto;
								margin-right: auto;
							}

							.makeTable {
								width: 100% !important;
							}

							.disabled {
								color: #666;
								cursor: not-allowed;
							}
						</style>
					</head>

					<body>

						<body class="hold-transition sidebar-mini">
							<div class="wrapper">
								<tiles:insertAttribute name="header" />

								<tiles:insertAttribute name="sideMenu" />


								<div class="content-wrapper">


									<form:form id="itemMasterForm1" method="POST" modelAttribute="itemMaster"
										action="${pageContext.request.contextPath}/add/itemMaster">
										<input type="hidden" name="party" id="party" />
										<input type="hidden" name="id" id="id" />
										<div id="salesDiv" class="card">
											<div class="container">
												<div class="row" style="padding: 10px;">
													<label for="itemModelNo"
														class="col-form-label form-control-sm col-sm-1">Model No</label>
													<div class="col-sm-2">
														<input type="text" name="model" id="itemModelNo"
															class="form-control form-control-sm">
														<div id="itemModelNoDiv"></div>
													</div>

													<label for="itemHSNCode"
														class="col-form-label form-control-sm col-sm-1">HSN</label>
													<div class="col-sm-2">
														<input type="text" name="hsnCode" id="itemHSNCode"
															class="form-control form-control-sm">
													</div>

													<label for="itemDescription"
														class="col-form-label form-control-sm col-sm-1.5">Description</label>
													<div class="col-sm-4">
														<input type="text" name="itemName" id="itemDescription"
															class="form-control form-control-sm">
														<div id="itemDescriptionDiv"></div>
													</div>
												</div>
												<div class="row" style="padding: 10px;">
													<label for="units"
														class="col-form-label form-control-sm col-sm-1">Units</label>
													<div class="col-sm-10">
														<select name="units" id="units"
															class="form-control form-control-sm select2">
															<option value="" selected>Not selected</option>
														</select>
														<div id="itemUnitsDiv"></div>
													</div>

													<label for="gst" class="col-form-label form-control-sm col-sm-1">Tax
														Rate</label>
													<div class="col-sm-2">
														<input type="text" name="gst" id="gst"
															class="form-control form-control-sm">
														<div id="taxRateDiv"></div>
													</div>
													<label for="location"
														class="col-form-label form-control-sm col-sm-1">Location</label>
													<div class="col-sm-2">
														<input type="text" name="location" id="location"
															class="form-control form-control-sm">
														<div id="locationDiv"></div>
													</div>
													<label for="units"
														class="col-form-label form-control-sm col-sm-1">Make</label>
													<div class="col-sm-2">
														<select name="make" id="makeDropdown"
															class="form-control form-control-sm select2">
															<option value="" selected>Not selected</option>
														</select>
														<div>
															<i class="add fa fa-plus-square" id="addMake"
																aria-hidden="true"></i>
														</div>
														<div id="makeDiv"></div>
													</div>


												</div>
												<div class="row" style="padding: 10px;">
													<label for="toolsTracker"
														class="col-form-label form-control-sm col-sm-1">Tools
														Tackles</label>
													<div class="col-sm-2">
														<input style="width: 10%;" type="checkbox" value=""
															name="toolTracker" id="toolTracker"
															class="form-control form-control-sm" />
														<div id="toolsTrackerDiv"></div>
													</div>
													<label for="companyAssets"
														class="col-form-label form-control-sm col-sm-1">Company
														Assets</label>
													<div class="col-sm-2">
														<input style="width: 10%;" type="checkbox" value=""
															name="companyAssets" id="companyAssets"
															class="form-control form-control-sm" />
														<div id="companyAssetsDiv"></div>
													</div>
													<label for="quantity"
														class="col-form-label form-control-sm col-sm-1 hideInput">Quantity</label>
													<div class="col-sm-2">
														<input type="text" id="stockQuantity"
															class="form-control form-control-sm hideInput" readonly />
													</div>
													<label for="costPrice"
														class="col-form-label form-control-sm col-sm-1 hideInput">Cost
														Price</label>
													<div class="col-sm-2">
														<input type="text" id="supplierCostPrice"
															class="form-control form-control-sm hideInput" readonly />
													</div>

												</div>

											</div>


											<div class="button-div-style" align="center">
												<button type="submit" id="saveItemMaster"
													class="btn btn-primary btn-sm btn-inline">Save</button>

												<button type="button" id="cncl"
													class="btn btn-primary btn-sm btn-inline">Cancel
												</button>
											</div>
										</div>
									</form:form>




									<div style="margin: 20px 20px 20px 20px;">

										<table id="itemMasterList" class='table table-bordered table-striped dataTable'
											style="width: 100%; margin-top: -15px; font-size:13px">

										</table>


									</div>


								</div>

								<tiles:insertAttribute name="footer" />
							</div>
							<!-- ./wrapper -->
							<!-- ./box-body -->

							<!--.stock modal-dialog -->
							<div class="modal fade" tabindex="-1" role="dialog" id="stockModel">
								<div class="modal-dialog" role="document">
									<div class="modal-content" style="width:800px">
										<div class="modal-header custom-box-header">
											<h4 class="modal-title" id="stockHeader">Add Stock </h4>
											<button type="button" class="close buttonDismiss" id="stockReset"
												data-dismiss="modal" aria-label="Close" style="outline: none;">
												<span aria-hidden="true">&times;</span>

											</button>

										</div>


										<div class="row">
											<div class="col-5">

												<form class="form" id="stockModelClientForm">
													<input type="hidden" name="stockMId" id="stockMId" />
													<input type="hidden" name="itemMId" id="stockMItemId" />
													<div class="modal-body no-padding"
														style="margin-top: 0px !important;">

														<div class="container">
															<div class="row" style="padding: 10px;">
																<label for="itemModelNo"
																	class="col-form-label form-control-sm col-sm-3">Client</label>
																<div class="col-sm-8">
																	<select name="stockclientName" id="stockclientName"
																		class="form-control form-control-sm select2">
																		<!-- <option value="" selected>Not selected</option>  -->
																	</select>
																	<div id="stockClientErrorDiv"></div>
																</div>
															</div>
															<div class="row" style="padding: 10px;">
																<label for="itemHSNCode"
																	class="col-form-label form-control-sm col-sm-3"
																	style="padding-left: 7px;">Quantity</label>
																<div class="col-sm-4">
																	<input type="text" name="stockClientquantity"
																		id="stockClientquantity"
																		class="form-control form-control-sm"
																		data-msg="Quantity is required" required
																		readonly>
																</div>
																<div id="qtyunits"></div>

															</div>
															<div class="row" style="padding: 10px;">
																<label for="itemDescription"
																	class="col-form-label form-control-sm col-sm-3">Store</label>
																<div class="col-sm-8">
																	<input type="text" name="stockstoreName"
																		id="stockstoreName"
																		class="form-control form-control-sm"
																		data-msg="Store Name is required" required
																		readonly>
																</div>
															</div>
															<div class="row" style="padding: 10px;">
																<label for="itemDescription"
																	class="col-form-label form-control-sm col-sm-3"
																	style="padding-left: 7px;">Location</label>
																<div class="col-sm-8">
																	<input type="text" name="stocklocationInStore"
																		id="stocklocationInStore"
																		class="form-control form-control-sm" readonly>
																</div>
															</div>


														</div>


													</div>

												</form>

											</div>
											<!--    <div class="col-2">
       <button type="submit" id="saveStock" class="btn btn-primary btn-sm btn-inline" style="margin-top: 128px;">Assign To</button>
       </div> -->
											<div class="col-5">
												<!--For assigning the client  -->
												<div>
													<form class="form" id="stockModelForm">
														<input type="hidden" name="stockId" id="stockId" />
														<input type="hidden" name="itemId" id="stockItemId" />
														<div class="modal-body no-padding"
															style="margin-top: 0px !important;">
															<div class="container">
																<div class="row" style="padding: 10px;">
																	<label for="itemModelNo"
																		class="col-form-label form-control-sm col-sm-3">Client</label>
																	<div class="col-sm-8">
																		<select name="clientName" id="clientName"
																			class="form-control form-control-sm select2">
																			<option value="" selected>Not selected
																			</option>
																		</select>
																		<div id="stockErrorDiv"></div>
																	</div>
																</div>
																<div class="row" style="padding: 10px;">
																	<label for="itemHSNCode"
																		class="col-form-label form-control-sm col-sm-3"
																		style="padding-left: 7px;">Quantity</label>
																	<div class="col-sm-4">
																		<input type="text" name="quantity" id="quantity"
																			class="form-control form-control-sm"
																			data-msg="Quantity is required" required>
																	</div>
																	<div id="assignQtyunits"></div>

																</div>
																<div class="row" style="padding: 10px;">
																	<label for="itemDescription"
																		class="col-form-label form-control-sm col-sm-3">Store</label>
																	<div class="col-sm-8">
																		<input type="text" name="storeName"
																			id="storeName"
																			class="form-control form-control-sm"
																			data-msg="Store Name is required" required>
																	</div>
																</div>
																<div class="row" style="padding: 10px;">
																	<label for="itemDescription"
																		class="col-form-label form-control-sm col-sm-3"
																		style="padding-left: 7px;">Location</label>
																	<div class="col-sm-8">
																		<input type="text" name="locationInStore"
																			id="locationInStore"
																			class="form-control form-control-sm">
																	</div>
																</div>


															</div>
															<div class="button-div-style" align="center">
																<button type="submit" id="assignTo"
																	class="btn btn-primary btn-sm btn-inline">Assign</button>

																<button type="button"
																	class="btn btn-primary btn-sm btn-inline buttonDismiss"
																	data-dismiss="modal" id="resetBtn">Cancel
																</button>


															</div>
														</div>
													</form>




												</div>
											</div>
										</div>



										<div style="margin: 20px 20px 20px 20px;">
											<table id="stockList" class='table table-bordered table-striped dataTable'
												style="width: 100%">
											</table>
										</div>
										<!--  <div class="modal-footer">
					          <button type="button" class="btn btn btn-primary btn-sm btn-inline buttonDismiss" data-dismiss="modal">Close</button>
					        </div> -->
									</div>
									<!-- /.modal-content -->
								</div>

							</div>
							<!-- /.stock modal-dialog -->

							<!--.supplier modal-dialog -->
							<div class="modal fade" tabindex="-1" role="dialog" id="supplierModal">
								<div class="modal-dialog" role="document">
									<div class="modal-content" style="width:800px">
										<div class="modal-header custom-box-header">
											<h4 class="modal-title" id="supplierHeader">Map Supplier</h4>
											<button type="button" class="close buttonDismiss" id="supplierReset"
												data-dismiss="modal" aria-label="Close" style="outline: none;">
												<span aria-hidden="true">&times;</span>

											</button>

										</div>

										<form class="form" id="supplierModelForm">
											<input type="hidden" name="supplierId" />
											<input type="hidden" id="itemId" name="itemId" />
											<div class="modal-body no-padding">

												<div class="container">
													<div class="row" style="padding: 10px;">
														<label for="supplierName"
															class="col-form-label form-control-sm col-sm-1">Supplier</label>
														<div class="col-sm-5">
															<select name="supplierName" id="supplierName"
																class="form-control form-control-sm select2">
																<option value="" selected>Not selected</option>
															</select>
															<div id="supplierErrorDiv"></div>
														</div>

														<label for="costPrice"
															class="col-form-label form-control-sm col-sm-2"
															style="padding-left: 37px;">Cost Price</label>
														<div class="col-sm-2">
															<input type="text" name="costPrice" id="costPrice"
																class="form-control form-control-sm" required
																data-msg="Cost Price is required">
														</div>
														<label for="preferred"
															class="col-form-label form-control-sm col-sm-1">Preferred</label>
														<div class="col-sm-1" style="text-align: center;">
															<input type="radio" name="preferred" id="preferred"
																class="form-control-sm">
														</div>
													</div>
												</div>

												<div class="button-div-style" align="center">
													<button type="submit" id="saveSupplier"
														class="btn btn-primary btn-sm btn-inline">Save</button>

													<button type="reset" class="btn btn-primary btn-sm btn-inline"
														id="resetSupplierBtn">Reset
													</button>
												</div>

											</div>

										</form>

										<div style="margin: 20px 20px 20px 20px;">

											<table id="supplierList"
												class='table table-bordered table-striped dataTable'
												style="width: 100%">

											</table>


										</div>

										<div class="modal-footer">
											<button type="button"
												class="btn btn btn-primary btn-sm btn-inline buttonDismiss"
												data-dismiss="modal">Close</button>
										</div>

									</div>
									<!-- /.modal-content -->
								</div>

							</div>
							<!-- /.supplier modal-dialog -->

							<!-- Reason modal starts -->
							<div class="modal fade" tabindex="-1" role="dialog" id="reasonModal">
								<div class="modal-dialog" role="document">
									<div class="modal-content" style="width:600px">
										<div class="modal-header custom-box-header">
											<h4 class="modal-title" id="reasonHeader">History</h4>
											<button type="button" class="close" data-dismiss="modal" aria-label="Close"
												style="outline: none;">
												<span aria-hidden="true">&times;</span>

											</button>

										</div>
										<div class="modal-body no-padding">

											<div class="container">
												<div class="row" style="padding: 10px;">
													<table class="table table-bordered table-striped"
														id="previousReasonTable">
														<thead>
															<tr>
																<th class="col" style="width: 50%">Date</th>
																<th class="col" style="width: 50%">Reason</th>
															</tr>
														</thead>
														<tbody>

														</tbody>
													</table>
												</div>
											</div>
										</div>

									</div>
									<!-- /.modal-content -->
								</div>

							</div>
							<!--/. Reason modal ends -->

							<!---model for adding and deleting make-->
							<div class="modal fade" id="makeModal" role="dialog">
								<div class="modal-dialog modal-lg" style="margin-left: 33%; margin-top: 0%;">
									<div class="modal-content" style="width: 520px;">
										<div class="modal-header modalHeaderStyle">
											<h6 class="modal-title" id="makeHeader">
												<b>Add/Delete Make</b>
											</h6>
											<button type="button" class="close buttonDismiss" style="float:right"
												data-dismiss="modal">&times;</button>
										</div>
										<form class="form" id="makeForm">
											<input type="hidden" name="id" id="typeId" />
											<div class="modal-body">
												<table id="makeTable"
													class="makeTable table table-bordered table-striped">
												</table>
												<table class="modalTableBar botHeader">
													<tr class="newRow">
														<td><b>&nbsp;&nbsp;&nbsp;Make &nbsp;</b></td>
														<td><input type="text" name="name" id="makeInput"
																style="width: 350px; margin-left: 10%;"
																class="newPartyTxtBox txtCaps" />
															<div id="errorDiv"></div>
														</td>
													</tr>
												</table>
											</div>
											<div class="modal-footer footerDivinType">
												<button id="saveMake" type="submit"
													class="btn btn-primary autoRefresh">Save</button>
												<button type="reset" class="btn btn-primary"
													id="makeResetBtn">Cancel</button>

											</div>
										</form>
									</div>
								</div>
							</div>
							<div class="modal fade" tabindex="-1" role="dialog" id="itemUploadModal">
								<div class="modal-dialog" role="document">
									<div class="modal-content" style="width:500px">
										<div class="modal-header custom-box-header-modal">
											<h5 class="modal-title" id="itemUploadModalHeader">Upload Item File</h5>
											<button type="button" class="close buttonDismiss" data-dismiss="modal"
												aria-label="Close" style="outline: none;">
												<span aria-hidden="true">&times;</span>

											</button>

										</div>
										<form id="itemUploadForm" style="padding: 5px; " enctype="multipart/form-data">
											<input type="file" name="file" id="file" accept=".xlsx, .xls" required>
											<button type="submit" class="btn btn-primary btn-sm">Upload</button>
										</form>
										<div id="response"></div>

										<%-- <h1>Upload Sales Order Excel</h1>
											<form id="uploadForm" enctype="multipart/form-data">
												<input type="file" name="file" id="file" accept=".xlsx, .xls">
												<button type="submit">Upload</button>
											</form>
											<div id="response"></div> --%>
									</div>
									<!-- /.modal-content -->
								</div>

							</div>

							<!-- /.make modal ends -->

						</body>

					</html>