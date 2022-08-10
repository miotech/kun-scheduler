package com.miotech.kun.metadata.core.model.filter;

import java.time.OffsetDateTime;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-08-08 17:52
 **/
public class FilterRule {
    private FilterRuleType type;
    private Boolean positive;
    private String rule;
    private Boolean deleted;
    private OffsetDateTime updateTime;

    public static final class FilterRuleBuilder {
        private FilterRuleType type;
        private Boolean positive;
        private String rule;
        private Boolean deleted;
        private OffsetDateTime updateTime;

        private FilterRuleBuilder() {
        }

        public static FilterRuleBuilder builder() {
            return new FilterRuleBuilder();
        }

        public FilterRuleBuilder withType(FilterRuleType type) {
            this.type = type;
            return this;
        }

        public FilterRuleBuilder withPositive(Boolean positive) {
            this.positive = positive;
            return this;
        }

        public FilterRuleBuilder withRule(String rule) {
            this.rule = rule;
            return this;
        }

        public FilterRuleBuilder withDeleted(Boolean deleted) {
            this.deleted = deleted;
            return this;
        }

        public FilterRuleBuilder withUpdateTime(OffsetDateTime updateTime) {
            this.updateTime = updateTime;
            return this;
        }

        public FilterRule build() {
            FilterRule filterRule = new FilterRule();
            filterRule.positive = this.positive;
            filterRule.updateTime = this.updateTime;
            filterRule.rule = this.rule;
            filterRule.type = this.type;
            filterRule.deleted = this.deleted;
            return filterRule;
        }
    }

    public FilterRuleType getType() {
        return type;
    }

    public Boolean getPositive() {
        return positive;
    }

    public String getRule() {
        return rule;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public OffsetDateTime getUpdateTime() {
        return updateTime;
    }

    @Override
    public String toString() {
        return "FilterRule{" +
                "type=" + type +
                ", positive=" + positive +
                ", rule='" + rule + '\'' +
                ", deleted=" + deleted +
                ", updateTime=" + updateTime +
                '}';
    }
}
