package com.miotech.kun.datadiscovery.model.entity.rdm;

import com.miotech.kun.datadiscovery.service.rdm.file.UploadFileType;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-06-23 16:06
 **/
@Data
@AllArgsConstructor
public class RefUploadFileDescription {
    private UploadFileType uploadFileType;
    private String fileName;


}
