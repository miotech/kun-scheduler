package com.miotech.kun.common.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: Melo
 * @created: 5/26/20
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdVO {

    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;
}
