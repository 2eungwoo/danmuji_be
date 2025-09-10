package com.back2basics.board.post.service;

import com.back2basics.board.post.port.in.PostCreateUseCase;
import com.back2basics.board.post.port.in.command.PostCreateCommand;
import com.back2basics.board.post.service.result.PostCreateResult;
import com.back2basics.board.post.service.utils.PostCreateProcessor;
import com.back2basics.infra.s3.dto.PresignedUploadCompleteInfo;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostCreateService implements PostCreateUseCase {
    private final PostCreateProcessor processor;

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "dashboard:dueSoonPosts", allEntries = true),
        @CacheEvict(value = "dashboard:highPriorityPosts", allEntries = true)
    })
    public PostCreateResult createPost(Long userId, Long projectId, Long stepId,
        String userIp, PostCreateCommand command, List<MultipartFile> files) throws IOException {

        return processor.createWithMultipart(userId, projectId, stepId, userIp, command, files);
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "dashboard:dueSoonPosts", allEntries = true),
        @CacheEvict(value = "dashboard:highPriorityPosts", allEntries = true)
    })
    public PostCreateResult createPostWithPresigned(Long userId, Long projectId, Long stepId,
        String userIp, PostCreateCommand command, List<PresignedUploadCompleteInfo> uploadedFiles) {

        return processor.createWithPresigned(userId, projectId, stepId, userIp, command, uploadedFiles);
    }

}
