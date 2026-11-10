package com.ncpl.sales.security;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.Filter;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import com.ncpl.sales.repository.RolePermissionRepo;
import com.ncpl.sales.repository.RoleRepo;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ArchiveChartSecurityTest {
    private static AnnotationConfigWebApplicationContext context;
    private static Filter security;

    @Configuration
    static class Beans {
        @Bean public UserRepo users() { return mock(UserRepo.class); }
        @Bean public EncryptedPasswordUtils passwords() { return mock(EncryptedPasswordUtils.class); }
        @Bean public RoleRepo roles() { return mock(RoleRepo.class); }
        @Bean public RolePermissionRepo permissions() { return mock(RolePermissionRepo.class); }
        @Bean public UserStatusFilter statusFilter() { return new UserStatusFilter(); }
        @Bean public LoginSuccessHandler successHandler() { return new LoginSuccessHandler(); }
        @Bean public UserService userService() {
            UserService service = mock(UserService.class);
            User user = new User();
            user.setUsername("test-user"); user.setPassword("unused"); user.setEnabled(true);
            when(service.findByUserName("test-user")).thenReturn(user);
            when(service.toUserDetails(user)).thenAnswer(call ->
                    new org.springframework.security.core.userdetails.User("test-user", "unused",
                            SecurityContextHolder.getContext().getAuthentication().getAuthorities()));
            return service;
        }
    }

    @BeforeClass public static void startSecurity() {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.register(Beans.class, SecurityConfig.class);
        context.refresh();
        security = context.getBean("springSecurityFilterChain", Filter.class);
    }

    @AfterClass public static void stopSecurity() {
        if (context != null) context.close();
        SecurityContextHolder.clearContext();
    }

    private int get(String path, String... permissions) throws Exception {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (String permission : permissions) authorities.add(new SimpleGrantedAuthority(permission));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
        request.setServletPath(path);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                new SecurityContextImpl(new UsernamePasswordAuthenticationToken("test-user", "unused", authorities)));
        request.setSession(session);
        MockHttpServletResponse response = new MockHttpServletResponse();
        security.doFilter(request, response, (req, res) -> res.getWriter().write("allowed"));
        return response.getStatus();
    }

    @Test public void eachArchivePageRequiresItsOwnPermissionIncludingTrailingSlash() throws Exception {
        String[] pages = {"/salesList_archived", "/purchase_archived", "/dc_archived", "/grn_archived"};
        String[] keys = {"SALES_ORDER", "PURCHASE_ORDER", "DELIVERY_CHALLAN", "GRN"};
        for (int page = 0; page < pages.length; page++) {
            for (String suffix : new String[]{"", "/"}) {
                assertEquals(403, get(pages[page] + suffix, "ARCHIVES_VIEW"));
                for (int grant = 0; grant < keys.length; grant++) {
                    assertEquals(pages[page] + " with " + keys[grant], page == grant ? 200 : 403,
                            get(pages[page] + suffix, "ARCHIVES_VIEW", "ARCHIVE_" + keys[grant]));
                }
            }
        }
    }

    @Test public void chartsAreIndependentOfSalesOrders() throws Exception {
        for (String path : new String[]{"/soChart", "/api/so_monthly_chart"}) {
            for (String suffix : new String[]{"", "/"}) {
                assertEquals(200, get(path + suffix, "CHARTS_VIEW"));
                assertEquals(403, get(path + suffix, "SALES_ORDER_VIEW"));
            }
        }
        assertEquals(200, get("/salesList", "SALES_ORDER_VIEW"));
        assertEquals(403, get("/salesList", "CHARTS_VIEW"));
    }

    @Test public void archivedDcPageCanLoadItsSharedDataEndpoint() throws Exception {
        assertEquals(200, get("/dcList/data", "ARCHIVE_DELIVERY_CHALLAN"));
        assertEquals(200, get("/dcList/data/", "DELIVERY_CHALLAN_VIEW"));
        assertEquals(403, get("/dcList/data", "ARCHIVE_PURCHASE_ORDER", "ARCHIVES_VIEW"));
    }
}
