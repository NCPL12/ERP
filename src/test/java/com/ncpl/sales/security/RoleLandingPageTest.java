package com.ncpl.sales.security;

import java.util.Arrays;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import static org.junit.Assert.*;

public class RoleLandingPageTest {
    @Test public void chartAndArchiveOnlyUsersLandOnAPageTheyCanOpen() throws Exception {
        String[][] grants = {{"CHARTS_VIEW", "/soChart"},
                {"ARCHIVE_SALES_ORDER", "/salesList_archived"},
                {"ARCHIVE_PURCHASE_ORDER", "/purchase_archived"},
                {"ARCHIVE_DELIVERY_CHALLAN", "/dc_archived"}, {"ARCHIVE_GRN", "/grn_archived"}};
        for (String[] grant : grants) {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setContextPath("/erp");
            MockHttpServletResponse response = new MockHttpServletResponse();
            new LoginSuccessHandler().onAuthenticationSuccess(request, response,
                    new UsernamePasswordAuthenticationToken("user", "unused", Arrays.asList(
                            new SimpleGrantedAuthority("ARCHIVES_VIEW"), new SimpleGrantedAuthority(grant[0]))));
            assertEquals(grant[0], "/erp" + grant[1], response.getRedirectedUrl());
        }
    }
}
