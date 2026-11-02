package com.ncpl.sales.service;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.security.access.expression.method.*;
import org.springframework.security.access.prepost.*;
import org.springframework.security.access.intercept.aopalliance.MethodSecurityInterceptor;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.aop.framework.ProxyFactory;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

class TallyExportMethodSecurityTest {
    @Test void bothPostsDenyRevokedEditPermissionBeforeCallingExporter() throws Exception {
        com.ncpl.sales.controller.SalesController target=new com.ncpl.sales.controller.SalesController();
        TallyPurchaseOrderService exporter=mock(TallyPurchaseOrderService.class);
        PurchaseOrderService orders=mock(PurchaseOrderService.class);
        when(orders.getPurchaseOrderListByDate(any(),any())).thenReturn(Collections.emptyList());
        when(exporter.getBusinessTimeZone()).thenReturn(TimeZone.getTimeZone("Asia/Kolkata"));
        when(exporter.exportAllPending(anyList())).thenReturn(new TallyPurchaseOrderService.ExportResult(0,0,0,0,Collections.emptyList()));
        ReflectionTestUtils.setField(target,"purchaseOrderService",orders);
        ReflectionTestUtils.setField(target,"tallyPurchaseOrderService",exporter);
        ReflectionTestUtils.setField(target,"convertDate",new com.ncpl.sales.util.DateConverterUtil());
        DefaultMethodSecurityExpressionHandler handler=new DefaultMethodSecurityExpressionHandler();
        ExpressionBasedPreInvocationAdvice advice=new ExpressionBasedPreInvocationAdvice();advice.setExpressionHandler(handler);
        MethodSecurityInterceptor interceptor=new MethodSecurityInterceptor();
        interceptor.setSecurityMetadataSource(new PrePostAnnotationSecurityMetadataSource(new ExpressionBasedAnnotationAttributeFactory(handler)));
        interceptor.setAccessDecisionManager(new AffirmativeBased(Collections.singletonList(new PreInvocationAuthorizationAdviceVoter(advice))));
        interceptor.setAuthenticationManager(auth->auth);interceptor.afterPropertiesSet();
        ProxyFactory factory=new ProxyFactory(target);factory.setProxyTargetClass(true);factory.addAdvice(interceptor);
        com.ncpl.sales.controller.SalesController secured=(com.ncpl.sales.controller.SalesController)factory.getProxy();
        try {
            for(String role:Arrays.asList("SALES","PURCHASE","ADMIN")) {
                SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("revoked","",Collections.singletonList(new SimpleGrantedAuthority(role))));
                assertThatThrownBy(()->secured.sendPurchaseOrdersToTally("01-10-2026","31-10-2026")).isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
                assertThatThrownBy(()->secured.importPurchaseOrdersToTally("01-10-2026","31-10-2026",new org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap())).isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
            }
            verify(exporter,never()).exportAllPending(anyList());
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("editor","",Collections.singletonList(new SimpleGrantedAuthority("PURCHASE_EDIT"))));
            assertThat(secured.sendPurchaseOrdersToTally("01-10-2026","31-10-2026").getStatusCodeValue()).isEqualTo(200);
            assertThat(secured.importPurchaseOrdersToTally("01-10-2026","31-10-2026",new org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap())).isEqualTo("redirect:/sales_report");
            verify(exporter,times(2)).exportAllPending(anyList());
        }finally{SecurityContextHolder.clearContext();}
    }
}
