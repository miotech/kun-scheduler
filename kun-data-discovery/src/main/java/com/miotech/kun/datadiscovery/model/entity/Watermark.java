package com.miotech.kun.datadiscovery.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: Melo
 * @created: 6/1/20
 */

@Data
@NoArgsConstructor
public class Watermark {
    /* Epoch Unix timestamp in milliseconds */
    Double time;
}
