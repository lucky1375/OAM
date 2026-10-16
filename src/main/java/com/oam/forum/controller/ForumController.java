package com.oam.forum.controller;

import com.oam.common.dto.ApiResponse;
import com.oam.forum.dto.*;
import com.oam.forum.service.ForumService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
public class ForumController {

    private final ForumService forumService;

    public ForumController(ForumService forumService) {
        this.forumService = forumService;
    }

    // ==================== 版块 ====================

    @GetMapping("/api/forum/sections")
    public ApiResponse<List<SectionDto>> listSections() {
        return ApiResponse.ok(forumService.listSections());
    }

    @PostMapping("/api/forum/sections")
    public ApiResponse<SectionDto> createSection(@Valid @RequestBody SectionCreateRequest request) {
        return ApiResponse.ok("版块创建成功", forumService.createSection(request));
    }

    @PutMapping("/api/forum/sections/{id}")
    public ApiResponse<SectionDto> updateSection(@PathVariable Long id,
                                                 @Valid @RequestBody SectionCreateRequest request) {
        return ApiResponse.ok("更新成功", forumService.updateSection(id, request));
    }

    @DeleteMapping("/api/forum/sections/{id}")
    public ApiResponse<Void> deleteSection(@PathVariable Long id) {
        forumService.deleteSection(id);
        return ApiResponse.ok("删除成功", null);
    }

    // ==================== 帖子 ====================

    @GetMapping("/api/forum/posts")
    public ApiResponse<List<PostDto>> listPosts(@RequestParam(required = false) Long sectionId,
                                                @RequestParam(required = false) String keyword) {
        return ApiResponse.ok(forumService.listPosts(sectionId, keyword));
    }

    @GetMapping("/api/forum/posts/{id}")
    public ApiResponse<PostDetailDto> getPost(@PathVariable Long id) {
        return ApiResponse.ok(forumService.getPost(id));
    }

    @PostMapping("/api/forum/posts")
    public ApiResponse<PostDto> createPost(@Valid @RequestBody PostCreateRequest request) {
        return ApiResponse.ok("发帖成功", forumService.createPost(request));
    }

    @PutMapping("/api/forum/posts/{id}")
    public ApiResponse<PostDto> updatePost(@PathVariable Long id, @Valid @RequestBody PostCreateRequest request) {
        return ApiResponse.ok("更新成功", forumService.updatePost(id, request));
    }

    @DeleteMapping("/api/forum/posts/{id}")
    public ApiResponse<Void> deletePost(@PathVariable Long id) {
        forumService.deletePost(id);
        return ApiResponse.ok("删除成功", null);
    }

    // ==================== 评论 / 点赞 ====================

    @PostMapping("/api/forum/posts/{id}/comments")
    public ApiResponse<CommentDto> addComment(@PathVariable Long id, @Valid @RequestBody CommentRequest request) {
        return ApiResponse.ok("评论成功", forumService.addComment(id, request));
    }

    @DeleteMapping("/api/forum/comments/{id}")
    public ApiResponse<Void> deleteComment(@PathVariable Long id) {
        forumService.deleteComment(id);
        return ApiResponse.ok("删除成功", null);
    }

    @PostMapping("/api/forum/posts/{id}/like")
    public ApiResponse<Integer> toggleLike(@PathVariable Long id) {
        return ApiResponse.ok(forumService.toggleLike(id));
    }
}
