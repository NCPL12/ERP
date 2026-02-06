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
						<!--bank icon is working for this version only-->
						<link rel="stylesheet"
							href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
						<style type="text/css">
							.hideIcon {
								display: none !important;
							}

							.badge {
								display: block;
								position: absolute;
								top: 1px;
								right: 13px;
								line-height: 16px;
								height: 16px;
								padding: 0 5px;
								font-family: Arial, sans-serif;
								color: white;
								text-shadow: 0 1px rgba(black, .25);
								border-radius: 10px;
								background-color: #007bff;
								;
							}

							.editParty {
								cursor: pointer;
							}

							.select2-container--default .select2-selection--single {
								border: 1px solid #ced4da !important;
								height: 24px !important;
							}

							.select2-container--default .select2-selection--single .select2-selection__rendered {
								margin-top: -5px !important;
							}

							.border-color {
								border-color: red !important;
							}

							.pushmenuLink {
								padding: 0.5rem 1rem;
								color: #495057;
							}

							.pushmenuLink:hover,
							.pushmenuLink:focus {
								text-decoration: none;
							}

							.main-sidebar .brand-link {
								align-items: center;
								overflow: hidden;
								padding-top: .75rem;
								padding-bottom: .75rem;
							}

							.sidebar-collapse .main-sidebar .brand-link {
								justify-content: center;
							}

							.main-sidebar .brand-link .logo-xs,
							.main-sidebar .brand-link .logo-xl {
								line-height: 0;
								align-items: center;
							}

							/* Default (Expanded): Show XL, Hide XS */
							.main-sidebar .brand-link .logo-xl {
								display: flex;
							}

							.main-sidebar .brand-link .logo-xs {
								display: none;
							}

							/* Collapsed: Show XS, Hide XL (Always, even on hover) */
							.sidebar-collapse .main-sidebar .brand-link .logo-xl {
								display: none !important;
							}

							.sidebar-collapse .main-sidebar .brand-link .logo-xs {
								display: flex !important;
								justify-content: center !important;
								width: 100% !important;
							}

							.sidebar-collapse .main-sidebar .brand-link img.brand-image {
								margin-right: 0;
							}

							.main-sidebar .brand-link .logo-xs img.brand-image {
								max-height: 35px;
								max-width: 35px;
							}

							.main-sidebar .brand-link .logo-xl img.brand-image {
								max-height: 35px;
								max-width: 170px;
							}

							.main-sidebar .user-panel {
								padding-left: .75rem;
								padding-right: .75rem;
							}

							.main-sidebar .user-panel .image {
								display: flex;
								align-items: center;
							}

							.main-sidebar .user-panel .image i {
								font-size: 2.1rem !important;
								line-height: 1;
							}

							.main-sidebar .user-panel .info {
								white-space: nowrap;
								overflow: hidden;
								text-overflow: ellipsis;
								line-height: 1.4;
							}

							.user-avatar-circle {
								width: 2.1rem;
								height: 2.1rem;
								border-radius: 50%;
								background-color: #007bff;
								color: #fff;
								display: flex;
								align-items: center;
								justify-content: center;
								font-weight: bold;
								font-size: 1.2rem;
								text-transform: uppercase;
							}

							/* Center user panel when sidebar is collapsed */
							.sidebar-collapse .main-sidebar .user-panel {
								justify-content: center !important;
							}

							.sidebar-collapse .main-sidebar .user-panel .info {
								display: none !important;
							}

							.sidebar-collapse .main-sidebar .user-panel .image {
								padding-left: 0 !important;
							}

							.main-sidebar .user-panel .info a {
								white-space: nowrap;
								overflow: hidden;
								text-overflow: ellipsis;
							}
						</style>
						<script type="text/javascript">
							$(document).ready(function () {
								$('body').addClass('sidebar-mini sidebar-mini-md').removeClass('sidebar-collapse');

								$('.select2').select2({ dropdownAutoWidth: true });

								var partyObj = "";

								if ('${partyObj}' != null && '${partyObj}' != "") {
									partyObj = $.parseJSON('${partyObj}');

									$("#addIcon").removeClass('hideIcon');
									$("#altAddress").removeClass('hideIcon');
									$("#bankIcon").removeClass('hideIcon');

								}

								//$(document).on("hover","#partyList",function(){
								$(".partyList").hover(function () {
									$(this).attr('title', 'Party List');
								});
								$(".addParty").hover(function () {
									$(this).attr('title', 'Add Party');
								});
								$("#alt-address").hover(function () {
									$(this).attr('title', 'Alternate Address');
								});
								$("#bankIcon").hover(function () {
									$(this).attr('title', 'Bank');
								});
								$(".editParty").hover(function () {
									$(this).attr('title', 'Edit Party');
								});
								$(".salesList").hover(function () {
									$(this).attr('title', 'Sales List');
								});
								$(".poList").hover(function () {
									$(this).attr('title', 'Purchase Order List');
								});
								$(".addPO").hover(function () {
									$(this).attr('title', 'Add Purchase Order');
								});
								$(".addSO").hover(function () {
									$(this).attr('title', 'Add Sales Order');
								});
								$(".grnList").hover(function () {
									$(this).attr('title', 'Grn List');
								});
								$(".addGrn").hover(function () {
									$(this).attr('title', 'Add Grn');
								});
								$(".addDc").hover(function () {
									$(this).attr('title', 'Add DC');
								});
								$(".dcList").hover(function () {
									$(this).attr('title', 'DC List');
								});
								$(".addInvoice").hover(function () {
									$(this).attr('title', 'Add Invoice');
								});
								$(".invoiceList").hover(function () {
									$(this).attr('title', 'Invoice List');
								});
							})

						</script>

					</head>

					<body>
						<!-- Navbar -->
						<nav class="main-header navbar navbar-expand navbar-white navbar-light">
							<!-- Left navbar links -->
							<ul class="navbar-nav">
								<li class="nav-item">
									<a data-widget="pushmenu" href="#"><i class="fas fa-bars pushmenuLink"></i></a>
								</li>
								<li class="nav-item d-none d-sm-inline-block pageHeader">${pageHeader}

								</li>
								<c:if test="${pageHeader == 'Party List'}">
									<li class="nav-item d-none d-sm-inline-block">
										<a href="${pageContext.request.contextPath}/party" class="nav-link addParty"><i
												class="fa fa-plus-square headerIconFont"></i></a>
									</li>
								</c:if>
								<c:if test="${pageHeader == 'Item Master'}">

									&nbsp;&nbsp;&nbsp;&nbsp; <li>

										<a href="${pageContext.request.contextPath}/toolTackles"
											class="nav-link addTools"><button class="btn btn-primary btn-sm btn-inline"
												type="submit">Tools Tackles
												<span class="caret"></span></button></a>

										<form method="get"
											action="<%=request.getContextPath()%>/stockModified/Download">



											<!--   <button class="btn btn-flat btn-sm btn-default button-download"  type="submit">
								         <i class="fa fa-fw fa-download"></i>Download
								    <span class="caret"></span></button> -->

										</form>
									</li>
									<li>
										<div id="buttons"></div>
									</li><!-- <li><button id="exl">Export to Excel</button></li> -->
									<%-- <li><a href="${pageContext.request.contextPath}/companyAssets"
											class="nav-link addTools"><button class="btn btn-primary btn-sm btn-inline"
												type="submit">Company Assets
												<span class="caret"></span></button></a></li> --%>
										<li class="nav-item d-none d-sm-inline-block">
											<button type='button'
												class='btn btn-default btn-flat btn-xs itemUploadButton'
												style='height: -webkit-fill-available'><i
													class='fa fa-fw fa-upload'></i> Items Upload</button>
										</li>
								</c:if>
								<c:if test="${pageHeader == 'Sales List'}">
									<li class="nav-item d-none d-sm-inline-block">
										<a href="${pageContext.request.contextPath}/new_salesOrder"
											class="nav-link addSO"><i class="fa fa-plus-square headerIconFont"></i></a>
									</li>
									<li class="nav-item d-none d-sm-inline-block">
										<button type='button' class='btn btn-default btn-flat btn-xs soUploadButton'
											style='height: -webkit-fill-available'><i class='fa fa-fw fa-upload'></i> SO
											Upload</button>
									</li>
								</c:if>
								<c:if test="${pageHeader == 'Purchase Orders'}">
									<li class="nav-item d-none d-sm-inline-block">
										<a href="${pageContext.request.contextPath}/purchaseOrder"
											class="nav-link addPO"><i class="fa fa-plus-square headerIconFont"></i></a>
									</li>
									<li class="nav-item d-none d-sm-inline-block">
										<form id="globalSearchPoForm"
											action="${pageContext.request.contextPath}/purchase_list/by_item_id"
											method="get" class="sidebar-form">
											<div class="input-group">
												<input type="text" name="itemId" id="itemId" class="form-control"
													placeholder="Search..." required="required">
												<span class="input-group-btn">
													<button type="submit" name="search" id="search-btn"
														class="btn btn-flat"><i class="fa fa-search"></i>
													</button>
												</span>
											</div>
										</form>
									</li>
									<li class="nav-item d-none d-sm-inline-block">
										<button type='button' class='btn btn-default btn-flat btn-xs poUploadButton'
											style='height: -webkit-fill-available'><i class='fa fa-fw fa-upload'></i> PO
											Upload</button>
									</li>
								</c:if>
								<c:if test="${pageHeader == 'GRN'}">
									<li class="nav-item d-none d-sm-inline-block">
										<a href="${pageContext.request.contextPath}/new_grn" class="nav-link"><i
												class="fa fa-plus-square addGrn headerIconFont"></i></a>
									</li>
									<li class="nav-item d-none d-sm-inline-block">
										<form id="globalSearchGrnForm"
											action="${pageContext.request.contextPath}/grn_list/by_item_id" method="get"
											class="sidebar-form">
											<div class="input-group">
												<input type="text" name="itemId" id="itemId" class="form-control"
													placeholder="Search..." required="required">
												<span class="input-group-btn">
													<button type="submit" name="search" id="search-btn"
														class="btn btn-flat"><i class="fa fa-search"></i>
													</button>
												</span>
											</div>
										</form>
									</li>
									<!-- <li class="form-inline float-sm-right dropdownAllign"><h1 class=" nav-item d-none d-sm-inline-block" style="font-size: x-large;">Purchase Order : &nbsp;</h1>
	        <select class="form-control select2 select2-hidden-accessible" name="poNumber" id="purchaseOrderDropDown" style="width:230px;height: 30px; padding: 1.5px; border-radius: 0;">
                 <option>Select Purchase Order</option>
					
				</select>	
	       </li> -->
								</c:if>
								<c:if test="${pageHeader == 'Sales Order'}">
									<li class="nav-item d-none d-sm-inline-block">
										<a href="${pageContext.request.contextPath}/salesList"
											class="nav-link salesList"><i class="fa fa-list-alt headerIconFont"></i></a>
									</li>
									<li class="form-inline"><span class="m-0 text-dark marginLeft7">Client PO :
											&nbsp;</span>
										<input type="text" name="clientPoNumber" id="clientPoNumber"
											class="form-control PositionofTextbox fieldwidth150" />
									</li>
									<li class="form-inline"><span class="m-0 text-dark marginLeft30">Client PO.Date :
											&nbsp;</span>
										<input type="text" name="clientPoDate" id="clientPoDate" readonly="readonly"
											class="form-control PositionofTextbox fieldwidth150" />
									</li>
									<li class="form-inline float-sm-right dropdownAllignTop15"><span
											class=" m-0 text-dark">Party : &nbsp;</span>
										<select id="partyDropDown"
											class="form-control select2 select2-hidden-accessible dropdownWidth230">
											<option selected value="">Select Party</option>
										</select>
									</li>
								</c:if>
								<c:if test="${pageHeader == 'Purchase Order'}">
									<li class="nav-item d-none d-sm-inline-block">
										<a href="${pageContext.request.contextPath}/purchase" class="nav-link poList"><i
												class="fa fa-list-alt headerIconFont"></i></a>
									</li>
									<li class="form-inline float-sm-right dropdownAllignTop15"><span
											class=" nav-item d-none d-sm-inline-block headerFont">Vendor : &nbsp;
										</span>
										<select name="partyByType" id="partyDropDown2"
											class="form-control select2 select2-hidden-accessible dropdownWidth230">
											<option value="">Select Vendor:</option>
										</select>
									</li>
									<li class="form-inline"><span class="m-0 text-dark marginLeft7">Auto PO :
											&nbsp;</span><span><input style="width: 100%;" type="checkbox" value=""
												name="autoPOcheckbox" id="autoPOcheckbox"
												class="form-control form-control-sm" /></span></li>
								</c:if>
								<c:if test="${pageHeader == 'Party'}">
									<li class="nav-item d-none d-sm-inline-block">
										<a href="${pageContext.request.contextPath}/partyList"
											class="nav-link partyList" id="partyList">
											<i class="fa fa-list-alt headerIconFont"></i>
										</a>
									</li>
									<li class="nav-item d-none d-sm-inline-block hideIcon" id="addIcon">
										<a href="${pageContext.request.contextPath}/party" class="nav-link addParty"
											id="addParty">
											<i class="fa fa-plus-square headerIconFont"></i>
										</a>
									</li>
									<li class="nav-item d-none d-sm-inline-block hideIcon" id="altAddress">
										<a href="${pageContext.request.contextPath}/partyAltAddress?partyId=${partyId}"
											class="nav-link" class="nav-link" id="alt-address">
											<span class="badge">${addressCount}</span>
											<i class="fa fa-address-card headerIconFont" aria-hidden="true"></i>
										</a>
									</li>
									<li class="nav-item d-none d-sm-inline-block hideIcon" id="bankIcon">
										<a href="${pageContext.request.contextPath}/partyBank?partyId=${partyId}"
											class="nav-link" class="nav-link" id="alt-address">
											<span class="badge">${bankDetailsCount}</span>
											<i class="fa fa-bank headerIconFont" aria-hidden="true"></i>
										</a>
									</li>
								</c:if>
								<c:if test="${pageHeader == 'Party Address'}">
									<li class="nav-item d-none d-sm-inline-block">
										<a href="${pageContext.request.contextPath}/partyList"
											class="nav-link partyList" id="partyList">
											<i class="fa fa-list-alt headerIconFont"></i>
										</a>
									</li>
									<li class="nav-item d-none d-sm-inline-block" id="addIcon">
										<a href="${pageContext.request.contextPath}/party" class="nav-link addParty"
											id="addParty">
											<i class="fa fa-plus-square headerIconFont"></i>
										</a>
									</li>
									<li class="nav-item d-none d-sm-inline-block">
										<a class="nav-link editParty" id="editParty">
											<i class="fa fa-user headerIconFont">
											</i>
										</a>
									</li>

									<li class="nav-item d-none d-sm-inline-block" id="bankIconAddr">
										<a href="${pageContext.request.contextPath}/partyBank?partyId=${partyId}"
											class="nav-link" class="nav-link" id="bank-alt-address">
											<span class="badge">${bankCountinAddress}</span>
											<i class="fa fa-bank headerIconFont" aria-hidden="true"></i>
										</a>
									</li>

								</c:if>
								<c:if test="${pageHeader == 'Party Bank'}">
									<li class="nav-item d-none d-sm-inline-block">
										<a href="${pageContext.request.contextPath}/partyList"
											class="nav-link partyList" id="partyList">
											<i class="fa fa-list-alt headerIconFont"></i>
										</a>
									</li>
									<li class="nav-item d-none d-sm-inline-block" id="addIcon">
										<a href="${pageContext.request.contextPath}/party" class="nav-link addParty"
											id="addParty">
											<i class="fa fa-plus-square headerIconFont"></i>
										</a>
									</li>
									<li class="nav-item d-none d-sm-inline-block">
										<a class="nav-link editParty" id="editPartyinBank">
											<i class="fa fa-user headerIconFont">
											</i>
										</a>
									</li>

									<li class="nav-item d-none d-sm-inline-block " id="altAddres">
										<a href="${pageContext.request.contextPath}/partyAltAddress?partyId=${partyId}"
											class="nav-link" class="nav-link" id="alt-address">
											<span class="badge">${addressCountinbank}</span>
											<i class="fa fa-address-card headerIconFont" aria-hidden="true"></i>
										</a>
									</li>

								</c:if>
								<c:if test="${pageHeader == 'New Grn'}">
									<li class="nav-item d-none d-sm-inline-block">
										<a href="${pageContext.request.contextPath}/grnLists"
											class="nav-link grnList"><i class="fa fa-list-alt headerIconFont"></i></a>
									</li>
									<li class="nav-item d-none d-sm-inline-block">
										<a href="${pageContext.request.contextPath}/new_grn" class="nav-link"><i
												id="addGRN"
												class="fa fa-plus-square addGrn hideIcon headerIconFont"></i></a>
									</li>

									<li class="form-inline"><span class="m-0 text-dark marginLeft7">PO.Date :
											&nbsp;</span>
										<input type="text" name="poDate" readonly="readonly" id="poDateVal"
											class="form-control PositionofTextbox" />
									</li>
									<li class="form-inline float-sm-right dropdownAllignTop15"><span
											class=" m-0 text-dark headerFont">Purchase Order : &nbsp;</span>

										<select class="form-control select2 select2-hidden-accessible dropdownWidth230"
											name="poNumber" id="purchaseOrderDropDown">
											<option value="">Select Purchase Order</option>

										</select>
									</li>
								</c:if>
								<c:if test="${pageHeader == 'Delivery Challan'}">
									<li class="nav-item d-none d-sm-inline-block">
										<a href="${pageContext.request.contextPath}/dcList" class="nav-link dcList"><i
												class="fa fa-list-alt headerIconFont"></i></a>
									</li>
									<li class="form-inline float-sm-right "><span
											class=" m-0 text-dark marginLeft7">Client : &nbsp;</span>
										<select class="form-control select2 select2-hidden-accessible dropdownWidth230"
											name="partyName" id="partyName">
											<option value="">Select Client Name</option>

										</select>
									</li>
									<li class="form-inline float-sm-right "><span
											class=" m-0 text-dark marginLeft7">Client PO Number : &nbsp;</span>

										<select class="form-control select2 select2-hidden-accessible dropdownwidth150"
											name="soNumber" id="clientPoDropdown">
											<option value="">Select Client PO No.</option>

										</select>
									</li>
									<li class="form-inline float-sm-right clientNamePos dropdownAllignTop15"><span
											class=" m-0 text-dark marginLeft30">Client Name : &nbsp;</span>

										<span id="clientName" class="m-0 text-dark"></span>
									</li>

								</c:if>
								<c:if test="${pageHeader == 'DC Dashboard'}">
									<li class="nav-item d-none d-sm-inline-block">
										<a href="${pageContext.request.contextPath}/deliveryChallan" class="nav-link"><i
												class="fa fa-plus-square addDc headerIconFont"></i></a>
									</li>

								</c:if>
								<c:if test="${pageHeader == 'Invoice'}">
									<li class="nav-item d-none d-sm-inline-block">
										<a href="${pageContext.request.contextPath}/invoiceList" class="nav-link"><i
												class="fa fa-list-alt invoiceList headerIconFont"></i></a>
									</li>
									<li class="form-inline float-sm-right "><span
											class=" m-0 text-dark marginLeft7">Client PO No. : &nbsp;</span>

										<select class="form-control select2 select2-hidden-accessible dropdownwidth150"
											name="soNumber" id="clientPoDropdown">
											<option value="">Select Client PO No.</option>
										</select>
									</li>

									<li class="form-inline float-sm-right"><span
											class=" m-0 text-dark marginLeft50">Type : &nbsp;</span>

										<select class="form-control select2 select2-hidden-accessible dropdownwidth150"
											name="type" id="typeDropdown">
											<option value="">Select Type</option>
											<option value="Service">Service</option>
											<option value="Supply">Supply</option>

										</select>
									</li>

									<li class="form-inline float-sm-right dropdownAllignTop15"><span
											class=" m-0 text-dark">DC : &nbsp;</span>

										<select class="form-control select2 select2-hidden-accessible dropdownwidth150"
											name="dcNumber" id="dcDropdown">
											<option value="">Select DC</option>

										</select>
									</li>

								</c:if>
								<c:if test="${pageHeader == 'Invoice Dashboard'}">
									<li class="nav-item d-none d-sm-inline-block">
										<a href="${pageContext.request.contextPath}/invoice" class="nav-link"><i
												class="fa fa-plus-square addInvoice headerIconFont"></i></a>
									</li>

								</c:if>
								<c:if test="${pageHeader == 'Sales Reports'}">
									<li class="nav-item d-none d-sm-inline-block">
										<a href="#" class="nav-link"></a>
									</li>

								</c:if>
								<c:if test="${pageHeader == 'Returnable Items'}">
									<li class="nav-item d-none d-sm-inline-block">
										<a href="${pageContext.request.contextPath}/returnableList"
											class="nav-link dcList"><i class="fa fa-list-alt headerIconFont"></i></a>
									</li>
								</c:if>

								<c:if test="${pageHeader == 'Work Order'}">
									<li class="nav-item d-none d-sm-inline-block">
										<a href="${pageContext.request.contextPath}/workOrderList"
											class="nav-link dcList"><i class="fa fa-list-alt headerIconFont"></i></a>
									</li>
									<li class="form-inline float-sm-right "><span
											class=" m-0 text-dark marginLeft7">Client PO Number : &nbsp;</span>

										<select class="form-control select2 select2-hidden-accessible dropdownwidth150"
											name="soNumber" id="clientPoDropdown">
											<option value="">Select Client PO No.</option>

										</select>
									</li>
									<li class="form-inline float-sm-right dropdownAllignTop15"><span
											class=" m-0 text-dark">Party : &nbsp;</span>
										<select id="partyDropDown"
											class="form-control select2 select2-hidden-accessible dropdownWidth230">
											<option selected value="">Select Party</option>
										</select>
									</li>
								</c:if>
								<c:if test="${pageHeader == 'Work Order List'}">
									<li class="nav-item d-none d-sm-inline-block">
										<a href="${pageContext.request.contextPath}/work_order" class="nav-link"><i
												class="fa fa-plus-square addDc headerIconFont"></i></a>
									</li>
								</c:if>
								<c:if test="${pageHeader == 'Non Billable'}">
									<li class="form-inline float-sm-right dropdownAllignTop15"><span
											class=" nav-item d-none d-sm-inline-block headerFont">Choose System : &nbsp;
										</span>
										<select name="nonBillable" id="nonBillableDropdown"
											class="form-control select2 select2-hidden-accessible dropdownWidth230">
											<option value="">Select System:</option>
										</select>
									</li>

								</c:if>
								<c:if test="${pageHeader == 'Purchase Order List By Item'}">
									<li class="form-inline float-sm-right dropdownAllignTop15"><span
											class=" nav-item d-none d-sm-inline-block headerFont">Total Accounted Qty :
											&nbsp; </span>
										<span id="accountedQty"></span>
									</li>

								</c:if>

								<c:if test="${pageHeader == 'Sales Order Monthly Chart'}">
									<li class="form-inline float-sm-right dropdownAllignTop15"><span
											class=" nav-item d-none d-sm-inline-block headerFont">Select Year : &nbsp;
										</span>
										<select name="soYear" id="soYear"
											class="form-control select2 select2-hidden-accessible dropdownWidth230">

											<option value="2019">2019-2020</option>
											<option value="2020">2020-2021</option>
											<option value="2021">2021-2022</option>
											<option value="2022">2022-2023</option>
											<option value="2023">2023-2024</option>
											<option value="2024">2024-2025</option>
											<option value="2025">2025-2026</option>
										</select>
									</li>

								</c:if>

							</ul>

						</nav>
						<!-- /.navbar -->
						<div class="faderv2">
							<div class="loaderv2">
								<img src="${RESOURCES}dist/img/loading-1.gif" class="loaderWidth">
							</div>
						</div>
					</body>

					</html>