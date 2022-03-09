package com.miotech.kun.metadata.core.model.search;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import com.miotech.kun.metadata.core.model.constant.SearchContent;
import com.miotech.kun.metadata.core.model.constant.SearchOperator;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * @program: kun
 * @description: SearchFilterOption
 * @author: zemin  huang
 * @create: 2022-03-09 09:28
 **/
public class SearchFilterOption {
    private SearchOperator searchOperator; //first option  is ignore
    private String keyword;
    private Set<SearchContent> searchContents;

    public SearchFilterOption(SearchOperator searchOperator, String keyword, Set<SearchContent> searchContents) {
        this.searchOperator = searchOperator;
        this.keyword = keyword;
        this.searchContents = searchContents;
    }

    public SearchOperator getSearchOperator() {
        return searchOperator;
    }

    public String getKeyword() {
        return keyword;
    }

    public Set<SearchContent> getSearchContents() {
        return searchContents;
    }

    public String getSearchContentsWeightString() {
        Set<Character> collect = searchContents.stream().map(SearchContent::getWeight).collect(Collectors.toSet());
        return Joiner.on("").join(collect);
    }

    public static final class Builder {
        private SearchOperator searchOperator = SearchOperator.AND;
        private String keyword;
        private Set<SearchContent> searchContents = Sets.newHashSet(SearchContent.values());

        private Builder() {
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public Builder withSearchOperator(SearchOperator searchOperator) {
            this.searchOperator = searchOperator;
            return this;
        }

        public Builder withKeyword(String keyword) {
            this.keyword = keyword;
            return this;
        }

        public Builder withSearchContents(Set<SearchContent> searchContents) {
            this.searchContents = searchContents;
            return this;
        }

        public SearchFilterOption build() {
            return new SearchFilterOption(searchOperator, keyword, searchContents);
        }
    }
}
