package com.back2basics.board.post.service;

import com.back2basics.board.post.model.Post;
import com.back2basics.board.post.port.in.PostDeleteUseCase;
import com.back2basics.board.post.port.out.PostSoftDeletePort;
import com.back2basics.history.model.DomainType;
import com.back2basics.history.service.HistoryLogService;
import com.back2basics.infra.validator.PostValidator;
import com.back2basics.infra.validator.UserValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostDeleteService implements PostDeleteUseCase {

    private final PostSoftDeletePort postSoftDeletePort;
    private final PostValidator postValidator;
    private final HistoryLogService historyLogService;
    private final UserValidator userValidator;

    @Override
    @Caching(evict = {
        @CacheEvict(value = "dashboard:dueSoonPosts", allEntries = true),
        @CacheEvict(value = "dashboard:highPriorityPosts", allEntries = true)
    })
    public void softDeletePost(Long requesterId, Long postId) {
        Post post = postValidator.findPost(postId);
        if(!userValidator.isAdmin(requesterId)){
            postValidator.isAuthor(post, requesterId);
        }

        post.markDeleted();

        historyLogService.logDeleted(DomainType.POST, requesterId, post, "게시글 비활성화");
        postSoftDeletePort.softDelete(post);
    }
}
