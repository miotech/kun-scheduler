package com.miotech.kun.dataplatform.facade;

import com.miotech.kun.dataplatform.facade.backfill.Backfill;

import java.util.Optional;

/**
 * Expose backfill interface
 */
public interface BackfillFacade {

    /**
     * Query by taskRunId
     * @param taskRunId
     * @return Wrapped optional ID object if found. Else returns empty optional.
     */
    Optional<Long> findDerivedFromBackfill(Long taskRunId);

    /**
     * create Backfill
     * @param backfill
     */
    void create(Backfill backfill);

}
