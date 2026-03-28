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
		
		if (methodName.equals("savesales") && args.length >= 2) {
			com.ncpl.sales.model.SalesOrder salesOrder = (com.ncpl.sales.model.SalesOrder) args[0];
			String partyId = args[1].toString();
			
			if (salesOrder != null) {
				salesOrderId = salesOrder.getId();
				if (salesOrder.getItems() != null) {
					itemsCount = salesOrder.getItems().size();
				}
			}
		}
		
		Object result = pjp.proceed();
		
		log.info(className + ":" + methodName + "()" + "Response");
		
		// Audit logging for sales item creation - only if auditService is available
		if (auditService != null && salesOrderId != null && itemsCount > 0) {
			try {
				HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
				String currentUser = getCurrentUsername();
				String clientIp = request.getRemoteAddr();
				
				// Create audit log for Sales Item creation
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
							java.util.Map.of("itemsCount", itemsCount, "salesOrderId", salesOrderId)
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

	private String getCurrentUsername() {
		try {
			return org.springframework.security.core.context.SecurityContextHolder.getContext()
					.getAuthentication().getName();
		} catch (Exception e) {
			return "SYSTEM";
		}
	}
}
