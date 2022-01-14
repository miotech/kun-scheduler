package com.miotech.kun.metadata.common.service;

import com.google.inject.Inject;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class TagServiceTest extends DatabaseTestBase {

    @Inject
    private TagService tagService;

    @Test
    public void testSearchTags_empty() {
        List<String> tags =
                tagService.searchTags(null);
        assertThat(tags.size(), is(0));
    }

}
