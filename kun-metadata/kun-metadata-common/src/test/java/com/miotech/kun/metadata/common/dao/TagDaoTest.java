package com.miotech.kun.metadata.common.dao;

import com.google.inject.Inject;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.metadata.common.factory.MockDatasetTagFactory;
import com.miotech.kun.metadata.core.model.dataset.DatasetTag;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;

import javax.sql.DataSource;
import java.util.List;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class TagDaoTest extends DatabaseTestBase {

    @Inject
    private DataSource dataSource;

    @Inject
    private TagDao tagDao;

    @AfterEach
    @Override
    public void tearDown() {
        super.tearDown();
        ((HikariDataSource) dataSource).close();
    }

    @Test
    public void testSave_empty() {
        List<String> tags = ImmutableList.of();
        tagDao.save(tags);

        List<String> tagsOfSearch = tagDao.searchTags(null);
        assertThat(tagsOfSearch, empty());
    }

    @Test
    public void testSave_new() {
        List<String> tags = ImmutableList.of("a", "b", "c");
        tagDao.save(tags);

        List<String> tagsOfSearch = tagDao.searchTags(null);
        assertThat(tagsOfSearch.size(), is(3));
        assertThat(tagsOfSearch, containsInAnyOrder("a", "b", "c"));
    }

    @Test
    public void testSave_duplicate() {
        List<String> tags = ImmutableList.of("a", "b", "c");
        tagDao.save(tags);

        List<String> tagsOfSearch = tagDao.searchTags(null);
        assertThat(tagsOfSearch.size(), is(3));
        assertThat(tagsOfSearch, containsInAnyOrder("a", "b", "c"));

        tagDao.save(tags);
        tagsOfSearch = tagDao.searchTags(null);
        assertThat(tagsOfSearch.size(), is(3));
        assertThat(tagsOfSearch, containsInAnyOrder("a", "b", "c"));
    }

    @Test
    public void testSearchTags_empty() {
        List<String> tags =
                tagDao.searchTags(null);
        assertThat(tags, empty());
    }

    @Test
    public void testSearchTags_createThenSearch() {
        List<String> tags = ImmutableList.of("a", "b", "c");
        tagDao.save(tags);

        List<String> tagsOfSearch =
                tagDao.searchTags("a");
        assertThat(tagsOfSearch.size(), is(1));
        assertThat(tagsOfSearch, contains("a"));
    }

    @Test
    public void testAddDatasetTag_blank() {
        Long gid = IdGenerator.getInstance().nextId();
        String tag = null;
        tagDao.addDatasetTag(gid, tag);

        List<DatasetTag> datasetTags = tagDao.findDatasetTags(gid);
        assertThat(datasetTags, empty());
    }

    @Test
    public void testAddDatasetTag() {
        Long gid = IdGenerator.getInstance().nextId();
        String tag = "tag_test";
        tagDao.addDatasetTag(gid, tag);

        List<DatasetTag> datasetTags = tagDao.findDatasetTags(gid);
        assertThat(datasetTags.size(), is(1));
        DatasetTag datasetTag = datasetTags.get(0);
        assertThat(datasetTag, sameBeanAs(new DatasetTag(IdGenerator.getInstance().nextId(), gid, tag)).ignoring("id"));
    }

    @Test
    public void testDeleteDatasetTags() {
        DatasetTag datasetTag = MockDatasetTagFactory.create();
        tagDao.addDatasetTag(datasetTag.getDatasetGid(), datasetTag.getTag());
        List<DatasetTag> datasetTags = tagDao.findDatasetTags(datasetTag.getDatasetGid());
        assertThat(datasetTags.size(), is(1));

        tagDao.deleteDatasetTags(datasetTag.getDatasetGid());

        datasetTags = tagDao.findDatasetTags(datasetTag.getDatasetGid());
        assertThat(datasetTags, empty());
    }

    @Test
    public void testOverwriteDatasetTags_emptyTags() {
        DatasetTag datasetTag = MockDatasetTagFactory.create();
        tagDao.addDatasetTag(datasetTag.getDatasetGid(), datasetTag.getTag());

        List<String> tags = ImmutableList.of();
        tagDao.overwriteDatasetTags(datasetTag.getDatasetGid(), tags);

        List<DatasetTag> datasetTags = tagDao.findDatasetTags(datasetTag.getDatasetGid());
        assertThat(datasetTags, empty());
    }

    @Test
    public void testOverwriteDatasetTags_blankTag() {
        DatasetTag datasetTag = MockDatasetTagFactory.create();
        tagDao.addDatasetTag(datasetTag.getDatasetGid(), datasetTag.getTag());

        List<String> tags = ImmutableList.of("");
        tagDao.overwriteDatasetTags(datasetTag.getDatasetGid(), tags);

        List<DatasetTag> datasetTags = tagDao.findDatasetTags(datasetTag.getDatasetGid());
        assertThat(datasetTags, empty());
    }

    @Test
    public void testOverwriteDatasetTags() {
        DatasetTag datasetTag = MockDatasetTagFactory.create();
        tagDao.addDatasetTag(datasetTag.getDatasetGid(), datasetTag.getTag());

        List<String> tags = ImmutableList.of("new_tag");
        tagDao.overwriteDatasetTags(datasetTag.getDatasetGid(), tags);

        List<DatasetTag> datasetTags = tagDao.findDatasetTags(datasetTag.getDatasetGid());
        assertThat(datasetTags.size(), is(1));
        DatasetTag datasetTagOfFetch = datasetTags.get(0);
        assertThat(datasetTagOfFetch.getTag(), is("new_tag"));
    }

}
