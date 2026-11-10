package com.ncpl.sales.service;

import com.ncpl.sales.util.NcplUtil;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class CompanyAddressFormatTest {
    @Test void billingAndDeliveryKeepConfiguredLinesInEveryProfile() throws Exception {
        String expected = "Neptune Controls Pvt Ltd\n"
                + "2nd Floor, No. 56/4, Sharada Towers Unit-II, Nandi Durga Road\n"
                + "Near Bharathi Airtel Office, Jayamahal Extension, Bengaluru Urban\n"
                + "Bengaluru\nKarnataka 560046";
        for (String profile : new String[]{"dev", "h2", "qa", "prod"}) {
            Properties config = new Properties();
            try (InputStream input = Files.newInputStream(Paths.get(
                    "src/main/resources/application-" + profile + ".properties"))) {
                config.load(input);
            }
            for (String property : new String[]{"billing.address", "shipping.address"}) {
                String[] addresses = config.getProperty(property).split("\\^\\^");
                assertThat(NcplUtil.formatCompanyAddress(addresses[0].split("\\$\\$")))
                        .as(profile + " " + property).isEqualTo(expected);
                String[] mangalore = addresses[1].split("\\$\\$");
                assertThat(NcplUtil.formatCompanyAddress(mangalore))
                        .isEqualTo(mangalore[1] + "\n" + mangalore[2] + "\n" + mangalore[3]
                                + "\nMangalore\nKarnataka " + mangalore[7]);
            }
        }
    }

    @Test void malformedConfigurationFailsInsteadOfPrintingAnIncompleteAddress() {
        assertThatIllegalArgumentException().isThrownBy(() ->
                NcplUtil.formatCompanyAddress(new String[]{"key1", "Company", "Street"}));
    }
}
