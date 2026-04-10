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
import com.ncpl.sales.service.SalesOrderAuditService;

@Aspect
@Component
public class LoggingAspect {

	private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

	@Autowired(required = false)
	private SalesOrderAuditService auditService;

	@Autowired(required = false)
	private ObjectMapper mapper;

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

	/**
	 * Sales line / SO audit is handled in {@link com.ncpl.sales.service.SalesService} (no duplicate
	 * CREATE_SALES_ITEM rows here).
	 */
	@Around("salesServiceSaveMethod()")
	public Object auditSalesServiceSaveMethod(ProceedingJoinPoint pjp) throws Throwable {
		String methodName = pjp.getSignature().getName();
		String className = pjp.getTarget().getClass().toString();
		log.info("Method Invoked {} : {}()", className, methodName);
		Object result = pjp.proceed();
		log.info("{}:{}() Response", className, methodName);
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
				
				// Create audit log for design save (aspect name is CREATE_DESIGN; runs on every save with items)
				com.ncpl.sales.model.SalesOrderAudit audit = new com.ncpl.sales.model.SalesOrderAudit();
				audit.setSalesOrderId(salesItemId);  // legacy: column holds sales item id for design rows
				audit.setSalesItemId(salesItemId);
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
