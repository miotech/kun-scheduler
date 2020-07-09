package com.miotech.kun.datadiscovery.model.bo;

import com.miotech.kun.datadiscovery.model.PageInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author: Melo
 * @created: 5/26/20
 */

@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
public class DatasetSearchRequest extends PageInfo {

    private String searchContent;

    private List<String> ownerList;

    private List<String> tagList;

    private List<Long> dbTypeList;

    private List<Long> dsIdList;

    private Long watermarkStart;

    private Long watermarkEnd;
}
