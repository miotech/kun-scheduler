package com.miotech.kun.datadiscovery.model.entity.rdm;

import com.miotech.kun.datadiscovery.service.rdm.file.UploadFileType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-06-23 17:44
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefInputSource {
    private RefUploadFileDescription refUploadFileDescription;
    private InputStream inputStream;

    public RefInputSource(MultipartFile file) throws IOException {
        this.inputStream = file.getInputStream();
        String name = file.getName();
        String endWith = name.substring(name.lastIndexOf("."));
        UploadFileType uploadFileType = UploadFileType.getFileType(endWith);
        this.refUploadFileDescription = new RefUploadFileDescription(uploadFileType, name);

    }
}
