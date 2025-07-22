package com.back2basics.file.service;

import com.back2basics.file.model.File;
import com.back2basics.file.port.in.FileDownloadUseCase;
import com.back2basics.file.port.out.FilePresignedUrlPort;
import com.back2basics.file.port.out.FileReadPort;
import com.back2basics.infra.s3.MimeTypeUtils;
import com.back2basics.infra.s3.S3Util;
import com.back2basics.infra.validator.FileValidator;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FileDownloadService implements FileDownloadUseCase {

    private final FileReadPort fileReadPort;
    private final FileValidator fileValidator;
    private final MimeTypeUtils mimeTypeUtils;
    private final S3Util s3Util;
    private final FilePresignedUrlPort presignedUrlPort;

    @Value("${cloudflare.r2.public-url}")
    private String publicUrlPrefix;

    @Override
    public FileDownloadResult downloadFile(Long userId, Long referenceId, Long fileId)
        throws IOException {

        fileValidator.validateDownloadPermission(userId, referenceId);
        File file = fileReadPort.getFileById(fileId);

        String key = extractKeyFromUrl(file.getFileUrl());
        File safeFile = applySafeMimeType(file);

        byte[] bytes = s3Util.downloadFile(key);

        return FileDownloadResult.toResult(safeFile, bytes);
    }

    @Override
    public FilePresignedUrlResult getPresignedDownloadUrl(Long userId, Long referenceId,
        Long fileId) {

        fileValidator.validateDownloadPermission(userId, referenceId);
        File file = fileReadPort.getFileById(fileId);

        String fileKey = file.getFileKey();
        String url = presignedUrlPort.generateDownloadUrl(fileKey);

        return new FilePresignedUrlResult(url);
    }

    private String extractKeyFromUrl(String fileUrl) {
        return fileUrl.replace("https://pub-2823e9b2fef94861b9d9cc553a84821b.r2.dev/", "");
    }

    private File applySafeMimeType(File file) {
        String safeType = mimeTypeUtils.getMimeType(file.getFileName(), file.getFileType());
        return file.withContentType(safeType);
    }

}
