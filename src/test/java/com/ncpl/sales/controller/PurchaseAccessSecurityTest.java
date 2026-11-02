package com.ncpl.sales.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Collections;

import org.junit.After;
import org.junit.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.ExpressionBasedPreInvocationAdvice;
import org.springframework.security.access.intercept.aopalliance.MethodSecurityInterceptor;
import org.springframework.security.access.prepost.PrePostAnnotationSecurityMetadataSource;
import org.springframework.security.access.expression.method.ExpressionBasedAnnotationAttributeFactory;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.access.prepost.PreInvocationAuthorizationAdviceVoter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import com.ncpl.sales.model.PurchaseOrder;
import com.ncpl.sales.service.PurchaseItemService;
import com.ncpl.sales.service.PurchaseOrderService;

public class PurchaseAccessSecurityTest {
    @After
    public void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void loginWith(String authority) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "test-user", "", Collections.singletonList(new SimpleGrantedAuthority(authority))));
    }

    private PurchaseController securedController(PurchaseOrderService orders, PurchaseItemService items) {
        PurchaseController target = new PurchaseController();
        ReflectionTestUtils.setField(target, "purchaseService", orders);
        ReflectionTestUtils.setField(target, "purchaseItemService", items);
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        ExpressionBasedPreInvocationAdvice advice = new ExpressionBasedPreInvocationAdvice();
        advice.setExpressionHandler(handler);
        MethodSecurityInterceptor interceptor = new MethodSecurityInterceptor();
        interceptor.setSecurityMetadataSource(new PrePostAnnotationSecurityMetadataSource(
                new ExpressionBasedAnnotationAttributeFactory(handler)));
        interceptor.setAccessDecisionManager(new AffirmativeBased(Collections.singletonList(
                new PreInvocationAuthorizationAdviceVoter(advice))));
        interceptor.setAuthenticationManager(authentication -> authentication);
        interceptor.afterPropertiesSet();
        ProxyFactory factory = new ProxyFactory(target);
        factory.setProxyTargetClass(true);
        factory.addAdvice(interceptor);
        return (PurchaseController) factory.getProxy();
    }

    private interface SecuredAction {
        void run() throws Exception;
    }

    private void assertDenied(SecuredAction action) throws Exception {
        try {
            action.run();
            fail("Expected access to be denied");
        } catch (AccessDeniedException expected) {
            // The controller body must not run.
        }
    }

    @Test
    public void purchaseWriteOperationsEnforceTheirOwnAuthorities() throws Exception {
        PurchaseOrderService orders = mock(PurchaseOrderService.class);
        PurchaseItemService items = mock(PurchaseItemService.class);
        PurchaseController controller = securedController(orders, items);
        MockMultipartFile emptyFile = new MockMultipartFile("file", new byte[0]);
        PurchaseOrder order = new PurchaseOrder();

        loginWith("PURCHASE_VIEW");
        assertDenied(() -> controller.displayEditPurchaseOrder("PO1", new ExtendedModelMap()));
        assertDenied(() -> controller.savePurchaseOrder(order, new MockHttpServletRequest(), new RedirectAttributesModelMap()));
        assertDenied(() -> controller.uploadExcel(emptyFile));
        assertDenied(() -> controller.deleteItem(7));
        assertDenied(() -> controller.updatePoArchive("PO1"));
        assertDenied(() -> controller.updatePoUnArchive("PO1"));

        loginWith("PURCHASE_EDIT");
        assertEquals(400, controller.uploadExcel(emptyFile).getStatusCodeValue());
        assertEquals("redirect:/purchase", controller.savePurchaseOrder(order,
                new MockHttpServletRequest(), new RedirectAttributesModelMap()));
        controller.updatePoArchive("PO1");
        controller.updatePoUnArchive("PO1");
        verify(orders).savePurchaseOrder(any(PurchaseOrder.class), isNull(), isNull());
        verify(orders).archivePO("PO1");
        verify(orders).unArchivePO("PO1");
        assertDenied(() -> controller.deleteItem(7));

        loginWith("PURCHASE_DELETE");
        assertEquals(200, controller.deleteItem(7).getStatusCodeValue());
        verify(items).deletePurchaseItem(7);
        assertDenied(() -> controller.uploadExcel(emptyFile));
    }
}
