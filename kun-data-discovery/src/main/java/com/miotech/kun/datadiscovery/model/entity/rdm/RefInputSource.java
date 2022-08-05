package com.miotech.kun.datadiscovery.model.entity.rdm;

import com.miotech.kun.datadiscovery.service.rdm.file.UploadFileType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-06-23 17:44
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class RefInputSource {
    private RefUploadFileDescription refUploadFileDescription;
    private InputStream inputStream;

    public RefInputSource(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            log.error("file upload is empty");
            throw new IllegalArgumentException("file upload is empty");
        }
        this.inputStream = file.getInputStream();
        String name = file.getOriginalFilename();
        String endWith = name.substring(name.lastIndexOf("."));
        UploadFileType uploadFileType = UploadFileType.getFileType(endWith);
        this.refUploadFileDescription = new RefUploadFileDescription(uploadFileType, name);
    }

    public void close() {
        if (Objects.nonNull(inputStream)) {
            try {
                inputStream.close();
            } catch (IOException e) {
                log.error("close error");
            }
        }
    }
}
