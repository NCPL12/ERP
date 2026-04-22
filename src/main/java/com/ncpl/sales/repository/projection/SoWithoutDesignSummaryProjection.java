package com.ncpl.sales.repository.projection;

import java.sql.Timestamp;

public interface SoWithoutDesignSummaryProjection {
    String getId();

    String getClientPoNumber();

    String getPartyName();

    Timestamp getCreated();

    Long getTotalItems();

    Long getItemsWithDesign();

    Long getPendingDesigns();
}
