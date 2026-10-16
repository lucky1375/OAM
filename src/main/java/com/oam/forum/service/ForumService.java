package com.oam.forum.service;

import com.oam.common.security.SecurityUtil;
import com.oam.forum.dto.*;
import com.oam.forum.entity.ForumComment;
import com.oam.forum.entity.ForumLike;
import com.oam.forum.entity.ForumPost;
import com.oam.forum.entity.ForumSection;
import com.oam.forum.repository.ForumCommentRepository;
import com.oam.forum.repository.ForumLikeRepository;
import com.oam.forum.repository.ForumPostRepository;
import com.oam.forum.repository.ForumSectionRepository;
import com.oam.user.entity.User;
import com.oam.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ForumService {

    private final ForumSectionRepository sectionRepository;
    private final ForumPostRepository postRepository;
    private final ForumCommentRepository commentRepository;
    private final ForumLikeRepository likeRepository;
    private final UserRepository userRepository;

    public ForumService(ForumSectionRepository sectionRepository, ForumPostRepository postRepository,
                        ForumCommentRepository commentRepository, ForumLikeRepository likeRepository,
                        UserRepository userRepository) {
        this.sectionRepository = sectionRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;
        this.userRepository = userRepository;
    }

    // ==================== 版块 ====================

    @Transactional(readOnly = true)
    public List<SectionDto> listSections() {
        return sectionRepository.findAllByOrderBySortAscIdAsc().stream()
                .map(s -> SectionDto.from(s, postRepository.countBySectionId(s.getId())))
                .collect(Collectors.toList());
    }

    @Transactional
    public SectionDto createSection(SectionCreateRequest request) {
        if (sectionRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("版块「" + request.getName() + "」已存在");
        }
        ForumSection s = new ForumSection();
        s.setName(request.getName());
        s.setDescription(request.getDescription());
        s.setSort(request.getSort() == null ? 0 : request.getSort());
        s.setCreatedBy(SecurityUtil.currentUsername());
        return SectionDto.from(sectionRepository.save(s));
    }

    @Transactional
    public SectionDto updateSection(Long id, SectionCreateRequest request) {
        ForumSection s = sectionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("版块不存在"));
        if (sectionRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new IllegalArgumentException("版块「" + request.getName() + "」已存在");
        }
        s.setName(request.getName());
        s.setDescription(request.getDescription());
        s.setSort(request.getSort() == null ? 0 : request.getSort());
        return SectionDto.from(sectionRepository.save(s));
    }

    @Transactional
    public void deleteSection(Long id) {
        ForumSection s = sectionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("版块不存在"));
        long postCount = postRepository.countBySectionId(id);
        if (postCount > 0) {
            throw new IllegalArgumentException("该版块下还有 " + postCount + " 个帖子，无法删除");
        }
        sectionRepository.delete(s);
    }

    // ==================== 帖子 ====================

    @Transactional(readOnly = true)
    public List<PostDto> listPosts(Long sectionId, String keyword) {
        List<ForumPost> posts;
        if (keyword != null && !keyword.trim().isEmpty()) {
            posts = postRepository.findByTitleContainingOrContentContainingOrderByCreatedAtDesc(
                    keyword.trim(), keyword.trim());
            if (sectionId != null) {
                posts = posts.stream().filter(p -> sectionId.equals(p.getSectionId())).collect(Collectors.toList());
            }
        } else if (sectionId != null) {
            posts = postRepository.findBySectionIdOrderByCreatedAtDesc(sectionId);
        } else {
            posts = postRepository.findAll().stream()
                    .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                    .collect(Collectors.toList());
        }
        return posts.stream()
                .map(p -> PostDto.from(p, sectionName(p.getSectionId())))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PostDetailDto getPost(Long id) {
        ForumPost p = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("帖子不存在"));
        List<CommentDto> comments = commentRepository.findByPostIdOrderByCreatedAtAsc(id).stream()
                .map(CommentDto::from).collect(Collectors.toList());
        Long userId = currentUserId();
        boolean liked = userId != null && likeRepository.existsByPostIdAndUserId(id, userId);
        return PostDetailDto.from(p, sectionName(p.getSectionId()), liked, comments);
    }

    @Transactional
    public PostDto createPost(PostCreateRequest request) {
        if (!sectionRepository.existsById(request.getSectionId())) {
            throw new IllegalArgumentException("版块不存在");
        }
        User user = currentUser();
        ForumPost p = new ForumPost();
        p.setSectionId(request.getSectionId());
        p.setTitle(request.getTitle().trim());
        p.setContent(request.getContent());
        p.setAuthorUserId(user != null ? user.getId() : null);
        p.setAuthorName(user != null ? displayName(user) : SecurityUtil.currentUsername());
        p.setLikeCount(0);
        p.setCommentCount(0);
        p.setCreatedBy(SecurityUtil.currentUsername());
        p = postRepository.save(p);
        return PostDto.from(p, sectionName(p.getSectionId()));
    }

    @Transactional
    public PostDto updatePost(Long id, PostCreateRequest request) {
        ForumPost p = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("帖子不存在"));
        if (request.getSectionId() != null && sectionRepository.existsById(request.getSectionId())) {
            p.setSectionId(request.getSectionId());
        }
        if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
            p.setTitle(request.getTitle().trim());
        }
        if (request.getContent() != null) {
            p.setContent(request.getContent());
        }
        p.setUpdatedBy(SecurityUtil.currentUsername());
        p = postRepository.save(p);
        return PostDto.from(p, sectionName(p.getSectionId()));
    }

    @Transactional
    public void deletePost(Long id) {
        if (!postRepository.existsById(id)) {
            throw new IllegalArgumentException("帖子不存在");
        }
        commentRepository.deleteByPostId(id);
        likeRepository.deleteByPostId(id);
        postRepository.deleteById(id);
    }

    // ==================== 评论 ====================

    @Transactional
    public CommentDto addComment(Long postId, CommentRequest request) {
        ForumPost p = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("帖子不存在"));
        User user = currentUser();
        ForumComment c = new ForumComment();
        c.setPostId(postId);
        c.setAuthorUserId(user != null ? user.getId() : null);
        c.setAuthorName(user != null ? displayName(user) : SecurityUtil.currentUsername());
        c.setContent(request.getContent().trim());
        c.setCreatedBy(SecurityUtil.currentUsername());
        c = commentRepository.save(c);
        p.setCommentCount((int) commentRepository.countByPostId(postId));
        postRepository.save(p);
        return CommentDto.from(c);
    }

    @Transactional
    public void deleteComment(Long id) {
        ForumComment c = commentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("评论不存在"));
        commentRepository.delete(c);
        postRepository.findById(c.getPostId()).ifPresent(p -> {
            p.setCommentCount((int) commentRepository.countByPostId(c.getPostId()));
            postRepository.save(p);
        });
    }

    // ==================== 点赞 ====================

    /** 点赞/取消点赞，返回最新点赞数 */
    @Transactional
    public int toggleLike(Long postId) {
        ForumPost p = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("帖子不存在"));
        User user = currentUser();
        if (user == null) {
            throw new IllegalArgumentException("登录用户才能点赞");
        }
        boolean existed = likeRepository.existsByPostIdAndUserId(postId, user.getId());
        if (existed) {
            likeRepository.findByPostIdAndUserId(postId, user.getId()).ifPresent(likeRepository::delete);
            p.setLikeCount(Math.max(0, p.getLikeCount() - 1));
        } else {
            ForumLike like = new ForumLike();
            like.setPostId(postId);
            like.setUserId(user.getId());
            likeRepository.save(like);
            p.setLikeCount(p.getLikeCount() + 1);
        }
        postRepository.save(p);
        return p.getLikeCount();
    }

    // ==================== 辅助 ====================

    private String sectionName(Long sectionId) {
        return sectionRepository.findById(sectionId).map(ForumSection::getName).orElse("已删除版块");
    }

    private User currentUser() {
        String username = SecurityUtil.currentUsername();
        if (username == null) return null;
        return userRepository.findByUsername(username).orElse(null);
    }

    private Long currentUserId() {
        User u = currentUser();
        return u == null ? null : u.getId();
    }

    private String displayName(User u) {
        return u.getNickname() != null && !u.getNickname().isEmpty() ? u.getNickname() : u.getUsername();
    }
}
