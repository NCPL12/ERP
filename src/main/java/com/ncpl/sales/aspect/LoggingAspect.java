package com.ncpl.sales.aspect;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ncpl.sales.model.SalesOrder;
import com.ncpl.sales.service.SalesOrderAuditService;

@Aspect
@Component
public class LoggingAspect {

	private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

	@Autowired(required = false)
	private SalesOrderAuditService auditService;

	@Autowired(required = false)
	private ObjectMapper mapper;

	@Pointcut(value = "execution(* com.ncpl.sales.service.SalesService.deleteSalesItemById(..))")
	public void salesServiceDeleteItemMethod() {
	}

	@Pointcut(value = "execution(* com.ncpl.sales.service.SalesService.savesales(..))")
	public void salesServiceSaveMethod() {
	}

	@Pointcut(value = "execution(* com.ncpl.sales.service.SalesOrderDesignService.save(..))")
	public void designServiceSaveMethod() {
	}

	@Pointcut(value = "execution(* com.ncpl.sales.service.PartyAddressService.updatePartyAddress(..))")
	public void partyAddressUpdateMethod() {
	}

	@Pointcut(value = "execution(* com.ncpl.sales.service.PartyAddressService.savePartyAddress(..))")
	public void partyAddressSaveMethod() {
	}

	@Around("salesServiceDeleteItemMethod()")
	public Object auditSalesServiceDeleteItemMethod(ProceedingJoinPoint pjp) throws Throwable {
		String methodName = pjp.getSignature().getName();
		Object[] args = pjp.getArgs();
		String className = pjp.getTarget().getClass().toString();
		
		log.info("Method Invoked " + className + " : " + methodName + "()" + " arguments");
		
		// Get sales item details before deletion for audit
		String salesItemId = null;
		String salesOrderId = null;
		String itemDescription = null;
		
		if (methodName.equals("deleteSalesItemById") && args.length > 0) {
			salesItemId = args[0].toString();
			
			// Try to get sales item details before deletion
			try {
				// Use reflection to call getSalesItemById method
				java.lang.reflect.Method getMethod = pjp.getTarget().getClass().getMethod("getSalesItemById", String.class, boolean.class);
				Object salesItemResult = getMethod.invoke(pjp.getTarget(), salesItemId, true);
				
				if (salesItemResult instanceof java.util.Optional) {
					java.util.Optional<?> salesItemOpt = (java.util.Optional<?>) salesItemResult;
					if (salesItemOpt.isPresent() && salesItemOpt.get() instanceof com.ncpl.sales.model.SalesItem) {
						com.ncpl.sales.model.SalesItem salesItem = (com.ncpl.sales.model.SalesItem) salesItemOpt.get();
						salesOrderId = salesItem.getSalesOrder() != null ? salesItem.getSalesOrder().getId() : "UNKNOWN_SO";
						itemDescription = salesItem.getDescription() != null ? 
							salesItem.getDescription() : "Unknown Item";
					}
				}
			} catch (Exception e) {
				log.warn("Could not retrieve sales item details for audit logging: " + e.getMessage());
				salesOrderId = "UNKNOWN_SO";
				itemDescription = "Unknown Item";
			}
		}
		
		Object result = pjp.proceed();
		
		log.info(className + ":" + methodName + "()" + "Response");
		
		// Audit logging for sales item deletion - only if auditService is available
		if (auditService != null && salesItemId != null) {
			try {
				HttpServletRequest request = getCurrentRequest();
				String performedBy = getCurrentUsername();
				
				// Create detailed audit entry for sales item deletion
				String description = "Deleted Sales Item: " + itemDescription + 
					" (ID: " + salesItemId + ") from Sales Order: " + salesOrderId;
				
				// Create audit log entry for sales item deletion
				com.ncpl.sales.model.SalesOrderAudit salesItemAudit = new com.ncpl.sales.model.SalesOrderAudit();
				salesItemAudit.setSalesOrderId(salesOrderId);
				salesItemAudit.setAction("DELETE_SALES_ITEM");
				salesItemAudit.setPerformedBy(performedBy);
				salesItemAudit.setActionPerformed(new java.sql.Timestamp(System.currentTimeMillis()));
				salesItemAudit.setDescription(description);
				salesItemAudit.setOldValues("{\"salesItemId\":\"" + salesItemId + "\",\"itemDescription\":\"" + itemDescription + "\"}");
				salesItemAudit.setNewValues("{\"status\":\"DELETED\"}");
				salesItemAudit.setIpAddress(request != null ? request.getRemoteAddr() : "UNKNOWN");
				salesItemAudit.setSessionId(request != null ? request.getSession().getId() : "UNKNOWN");
				
				auditService.saveAuditLog(salesItemAudit);
				
			} catch (Exception e) {
				log.error("Error in audit logging for " + methodName, e);
			}
		}
		
		return result;
	}

	private HttpServletRequest getCurrentRequest() {
		try {
			ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
			return attributes.getRequest();
		} catch (Exception e) {
			return null;
		}
	}

	@Around("salesServiceSaveMethod()")
	public Object auditSalesServiceSaveMethod(ProceedingJoinPoint pjp) throws Throwable {
		String methodName = pjp.getSignature().getName();
		Object[] args = pjp.getArgs();
		String className = pjp.getTarget().getClass().toString();
		
		log.info("Method Invoked " + className + " : " + methodName + "()" + " arguments");
		
		// Get sales order details for audit
		String salesOrderId = null;
		int itemsCount = 0;
		boolean isUpdate = false;
		
		if (methodName.equals("savesales") && args.length >= 2) {
			com.ncpl.sales.model.SalesOrder salesOrder = (com.ncpl.sales.model.SalesOrder) args[0];
			String partyId = args[1].toString();
			
			if (salesOrder != null) {
				// Check if this is an update (ID is not empty)
				isUpdate = salesOrder.getId() != null && !salesOrder.getId().isEmpty();
				
				// Get items count for audit logging
				if (salesOrder.getItems() != null) {
					itemsCount = salesOrder.getItems().size();
				}
			}
		}
		
		Object result = pjp.proceed();
		
		log.info(className + ":" + methodName + "()" + "Response");
		
		// Get sales order ID from result after save operation
		if (methodName.equals("savesales") && result instanceof com.ncpl.sales.model.SalesOrder) {
			com.ncpl.sales.model.SalesOrder savedSalesOrder = (com.ncpl.sales.model.SalesOrder) result;
			if (savedSalesOrder != null && savedSalesOrder.getId() != null) {
				salesOrderId = savedSalesOrder.getId();
			}
		}
		
		// Only log audit for CREATION - let service layer handle UPDATES
		// This prevents duplicate logging and allows proper old/new value tracking
		if (auditService != null && salesOrderId != null && !isUpdate && itemsCount > 0) {
			try {
				HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
				String currentUser = getCurrentUsername();
				String clientIp = request.getRemoteAddr();
				
				// Create audit log for Sales Item creation ONLY
				com.ncpl.sales.model.SalesOrderAudit audit = new com.ncpl.sales.model.SalesOrderAudit();
				audit.setSalesOrderId(salesOrderId);
				audit.setAction("CREATE_SALES_ITEM");
				audit.setPerformedBy(currentUser);
				audit.setActionPerformed(new java.sql.Timestamp(System.currentTimeMillis()));
				audit.setIpAddress(clientIp);
				audit.setSessionId(request.getSession().getId());
				
				// Set old and new values
				if (mapper != null) {
					try {
						String oldValues = mapper.writeValueAsString(null);
						String newValues = mapper.writeValueAsString(
							java.util.Map.of("itemsCount", itemsCount, "salesOrderId", salesOrderId != null ? salesOrderId : "")
						);
						audit.setOldValues(oldValues);
						audit.setNewValues(newValues);
					} catch (Exception e) {
						log.warn("Could not serialize audit data: " + e.getMessage());
					}
				}
				
				audit.setDescription("Created " + itemsCount + " Sales Items in Sales Order: " + salesOrderId);
				
				// Save audit log
				auditService.saveAuditLog(audit);
				log.info("Audit log created for Sales Item creation: " + salesOrderId);
				
			} catch (Exception e) {
				log.error("Error creating audit log for Sales Item creation: " + e.getMessage());
			}
		}
		
		return result;
	}

	@Around("designServiceSaveMethod()")
	public Object auditDesignServiceSaveMethod(ProceedingJoinPoint pjp) throws Throwable {
		String methodName = pjp.getSignature().getName();
		Object[] args = pjp.getArgs();
		String className = pjp.getTarget().getClass().toString();
		
		log.info("Method Invoked " + className + " : " + methodName + "()" + " arguments");
		
		// Get design details for audit
		String salesItemId = null;
		int designItemsCount = 0;
		
		if (methodName.equals("save") && args.length > 0) {
			com.ncpl.sales.model.SalesOrderDesign design = (com.ncpl.sales.model.SalesOrderDesign) args[0];
			
			if (design != null) {
				salesItemId = design.getSalesItemId();
				if (design.getItems() != null) {
					designItemsCount = design.getItems().size();
				}
			}
		}
		
		Object result = pjp.proceed();
		
		log.info(className + ":" + methodName + "()" + "Response");
		
		// Audit logging for design creation - only if auditService is available
		if (auditService != null && salesItemId != null && designItemsCount > 0) {
			try {
				HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
				String currentUser = getCurrentUsername();
				String clientIp = request.getRemoteAddr();
				
				// Create audit log for Design creation
				com.ncpl.sales.model.SalesOrderAudit audit = new com.ncpl.sales.model.SalesOrderAudit();
				audit.setSalesOrderId(salesItemId);  // Use sales item ID as reference
				audit.setAction("CREATE_DESIGN");
				audit.setPerformedBy(currentUser);
				audit.setActionPerformed(new java.sql.Timestamp(System.currentTimeMillis()));
				audit.setIpAddress(clientIp);
				audit.setSessionId(request.getSession().getId());
				
				// Set old and new values
				if (mapper != null) {
					try {
						String oldValues = mapper.writeValueAsString(null);
						String newValues = mapper.writeValueAsString(
							java.util.Map.of("designItemsCount", designItemsCount, "salesItemId", salesItemId)
						);
						audit.setOldValues(oldValues);
						audit.setNewValues(newValues);
					} catch (Exception e) {
						log.warn("Could not serialize audit data: " + e.getMessage());
					}
				}
				
				audit.setDescription("Created Design with " + designItemsCount + " items for Sales Item: " + salesItemId);
				
				// Save audit log
				auditService.saveAuditLog(audit);
				log.info("Audit log created for Design creation: " + salesItemId);
				
			} catch (Exception e) {
				log.error("Error creating audit log for Design creation: " + e.getMessage());
			}
		}
		
		return result;
	}

	@Around("partyAddressUpdateMethod()")
	public Object auditPartyAddressUpdateMethod(ProceedingJoinPoint pjp) throws Throwable {
		String methodName = pjp.getSignature().getName();
		Object[] args = pjp.getArgs();
		String className = pjp.getTarget().getClass().toString();
		
		log.info("#### PartyAddress UPDATE method invoked: " + className + " : " + methodName + "()");
		
		com.ncpl.sales.model.PartyAddress newPartyAddress = (com.ncpl.sales.model.PartyAddress) args[0];
		String addressId = newPartyAddress.getId();
		
		log.info("#### addressId = " + addressId);
		
		com.ncpl.sales.model.PartyAddress oldPartyAddress = null;
		if (addressId != null && !addressId.isEmpty()) {
			try {
				java.lang.reflect.Method getMethod = pjp.getTarget().getClass().getMethod("getAddressByAddressId", String.class);
				java.util.Optional<?> result = (java.util.Optional<?>) getMethod.invoke(pjp.getTarget(), addressId);
				if (result.isPresent()) {
					oldPartyAddress = (com.ncpl.sales.model.PartyAddress) result.get();
				}
			} catch (Exception e) {
				log.warn("Could not retrieve old PartyAddress for audit logging: " + e.getMessage());
			}
		}
		
		String oldValuesJson = null;
		String newValuesJson = null;
		
		if (mapper != null && oldPartyAddress != null) {
			try {
				oldValuesJson = mapper.writeValueAsString(java.util.Map.of(
					"addr1", oldPartyAddress.getAddr1() != null ? oldPartyAddress.getAddr1() : "",
					"addr2", oldPartyAddress.getAddr2() != null ? oldPartyAddress.getAddr2() : "",
					"city", oldPartyAddress.getPartyaddr_city() != null ? oldPartyAddress.getPartyaddr_city().getName() : "",
					"phone1", oldPartyAddress.getPhone1() != null ? oldPartyAddress.getPhone1() : "",
					"email1", oldPartyAddress.getEmail1() != null ? oldPartyAddress.getEmail1() : ""
				));
				newValuesJson = mapper.writeValueAsString(java.util.Map.of(
					"addr1", newPartyAddress.getAddr1() != null ? newPartyAddress.getAddr1() : "",
					"addr2", newPartyAddress.getAddr2() != null ? newPartyAddress.getAddr2() : "",
					"city", newPartyAddress.getPartyaddr_city() != null ? newPartyAddress.getPartyaddr_city().getName() : "",
					"phone1", newPartyAddress.getPhone1() != null ? newPartyAddress.getPhone1() : "",
					"email1", newPartyAddress.getEmail1() != null ? newPartyAddress.getEmail1() : ""
				));
			} catch (Exception e) {
				log.warn("Could not serialize audit data: " + e.getMessage());
			}
		}
		
		Object result = pjp.proceed();
		
		log.info(className + ":" + methodName + "()" + "Response");
		
		log.info("#### auditService is: " + (auditService != null ? "NOT NULL" : "NULL"));
		
		if (auditService != null) {
			log.info("#### Attempting to save audit for Party Address update");
			log.info("#### addressId = " + addressId);
			log.info("#### oldPartyAddress partyName = " + (oldPartyAddress != null && oldPartyAddress.getParty() != null ? oldPartyAddress.getParty().getPartyName() : "null"));
			try {
				HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
				String currentUser = getCurrentUsername();
				String clientIp = request.getRemoteAddr();
				
				com.ncpl.sales.model.SalesOrderAudit audit = new com.ncpl.sales.model.SalesOrderAudit();
				audit.setSalesOrderId(addressId);
				audit.setAction("UPDATE_ADDRESS");
				audit.setPerformedBy(currentUser);
				audit.setActionPerformed(new java.sql.Timestamp(System.currentTimeMillis()));
				audit.setIpAddress(clientIp);
				audit.setSessionId(request.getSession().getId());
				audit.setOldValues(oldValuesJson);
				audit.setNewValues(newValuesJson);
				audit.setDescription("Updated Party Address: " + addressId);
				
				auditService.saveAuditLog(audit);
				log.info("Audit log created for Party Address update: " + addressId);
				
			} catch (Exception e) {
				log.error("Error creating audit log for Party Address update: " + e.getMessage());
			}
		}
		
		return result;
	}

	@Around("partyAddressSaveMethod()")
	public Object auditPartyAddressSaveMethod(ProceedingJoinPoint pjp) throws Throwable {
		String methodName = pjp.getSignature().getName();
		Object[] args = pjp.getArgs();
		String className = pjp.getTarget().getClass().toString();
		
		log.info("#### PartyAddress SAVE method invoked: " + className + " : " + methodName + "()");
		
		com.ncpl.sales.model.PartyAddress newPartyAddress = (com.ncpl.sales.model.PartyAddress) args[0];
		
		log.info("#### Party ID = " + (newPartyAddress.getParty() != null ? newPartyAddress.getParty().getId() : "null"));
		
		String newValuesJson = null;
		if (mapper != null) {
			try {
				newValuesJson = mapper.writeValueAsString(java.util.Map.of(
					"addr1", newPartyAddress.getAddr1() != null ? newPartyAddress.getAddr1() : "",
					"addr2", newPartyAddress.getAddr2() != null ? newPartyAddress.getAddr2() : "",
					"city", newPartyAddress.getPartyaddr_city() != null ? newPartyAddress.getPartyaddr_city().getName() : "",
					"phone1", newPartyAddress.getPhone1() != null ? newPartyAddress.getPhone1() : "",
					"email1", newPartyAddress.getEmail1() != null ? newPartyAddress.getEmail1() : ""
				));
			} catch (Exception e) {
				log.warn("Could not serialize audit data: " + e.getMessage());
			}
		}
		
		Object result = pjp.proceed();
		
		log.info(className + ":" + methodName + "()" + "Response");
		
		log.info("#### auditService is: " + (auditService != null ? "NOT NULL" : "NULL"));
		
		if (auditService != null) {
			log.info("#### Attempting to save audit for Party Address save");
			try {
				HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
				String currentUser = getCurrentUsername();
				String clientIp = request.getRemoteAddr();
				
				com.ncpl.sales.model.PartyAddress savedAddress = (com.ncpl.sales.model.PartyAddress) result;
				String savedAddressId = savedAddress != null ? savedAddress.getId() : "UNKNOWN";
				
				com.ncpl.sales.model.SalesOrderAudit audit = new com.ncpl.sales.model.SalesOrderAudit();
				audit.setSalesOrderId(savedAddressId);
				audit.setAction("CREATE_ADDRESS");
				audit.setPerformedBy(currentUser);
				audit.setActionPerformed(new java.sql.Timestamp(System.currentTimeMillis()));
				audit.setIpAddress(clientIp);
				audit.setSessionId(request.getSession().getId());
				audit.setOldValues(null);
				audit.setNewValues(newValuesJson);
				audit.setDescription("Created New Party Address for: " + (newPartyAddress.getParty() != null ? newPartyAddress.getParty().getPartyName() : "Unknown Party"));
				
				auditService.saveAuditLog(audit);
				log.info("Audit log created for Party Address creation: " + savedAddressId);
				
			} catch (Exception e) {
				log.error("Error creating audit log for Party Address creation: " + e.getMessage());
			}
		}
		
		return result;
	}

	private String getCurrentUsername() {
		try {
			return org.springframework.security.core.context.SecurityContextHolder.getContext()
					.getAuthentication().getName();
		} catch (Exception e) {
			return "SYSTEM";
		}
	}
}
