package com.miotech.kun.metadata.databuilder.extract;

import com.miotech.kun.commons.utils.Props;

public abstract class AbstractExtractor implements Extractor {

    private final Props props;

    public AbstractExtractor(Props props) {
        this.props = props;
    }

    public Props getProps() {
        return props;
    }
}
