package com.miotech.kun.datadiscovery.model.entity.rdm;

import com.miotech.kun.datadiscovery.model.enums.ConstraintType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-06-23 16:09
 **/
@Data
@NoArgsConstructor
public class RefTableMetaData {
    private LinkedHashSet<RefColumn> columns;
    private LinkedHashMap<ConstraintType, Set<String>> refTableConstraints;
}
