package com.miotech.kun.datadiscover.model.bo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author: Melo
 * @created: 5/26/20
 */

@Data
@NoArgsConstructor
public class DatasetRequest {

    String description;

    List<String> owners;

    List<String> tags;
}
