package com.back2basics.board.post.service;

import com.back2basics.board.post.model.Post;
import com.back2basics.board.post.port.in.PostReadUseCase;
import com.back2basics.board.post.port.out.PostReadPort;
import com.back2basics.board.post.service.result.PostDashboardReadResult;
import com.back2basics.board.post.service.result.PostDetailReadResult;
import com.back2basics.board.post.service.result.PostSummaryReadResult;
import com.back2basics.board.post.service.result.ReadRecentPostResult;
import com.back2basics.global.cache.DashboardCacheService;
import com.back2basics.infra.validator.PostValidator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostReadService implements PostReadUseCase {

    private final PostReadPort postReadPort;
    private final PostValidator postValidator;
    private final DashboardCacheService dashboardCacheService; // 의존성 추가

    @Override
    public PostDetailReadResult getPostById(Long userId, Long postId) {
        Post post = postValidator.findPost(postId);
        return PostDetailReadResult.toResult(post);
    }


    @Override
    public Page<PostSummaryReadResult> getAllPostsByProjectIdAndStepId(Long userId, Long projectId,
        Long projectStepId,
        Pageable pageable) {
        return postReadPort.findAllPostsByProjectIdAndStepId(projectId, projectStepId, pageable)
            .map(PostSummaryReadResult::toResult);
    }

    @Override
    public List<ReadRecentPostResult> getRecentPosts() {
        return postReadPort.getRecentPosts();
    }

    @Override
    @Cacheable(value = "dashboard", key = "@dashboardCacheService.generateKey(#userId, 'dueSoonPosts')")
    public List<PostDashboardReadResult> getPostsWithProjectIdAndDueSoon(Long userId) {
        return postReadPort.getPostsWithProjectIdAndDueSoon(userId).stream()
            .map(PostDashboardReadResult::toResult)
            .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "dashboard", key = "@dashboardCacheService.generateKey(#userId, 'highPriorityPosts')")
    public List<PostDashboardReadResult> getHighPriorityPostsByUserId(Long userId) {
        return postReadPort.getHighPriorityPostsByUserId(userId).stream()
            .map(PostDashboardReadResult::toResult)
            .toList();
    }
}
