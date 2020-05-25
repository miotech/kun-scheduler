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
public class DatabaseRequest {

    String type;

    String name;

    String ip;

    String username;

    String password;

    List<String> tags;
}
