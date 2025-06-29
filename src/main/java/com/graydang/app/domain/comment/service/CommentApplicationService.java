package com.graydang.app.domain.comment.service;

import com.graydang.app.domain.auth.oauth2.CustomUserDetails;
import com.graydang.app.domain.bill.model.Bill;
import com.graydang.app.domain.bill.service.BillService;
import com.graydang.app.domain.comment.mapper.CommentMapper;
import com.graydang.app.domain.comment.model.Comment;
import com.graydang.app.domain.comment.model.dto.CommentReadResponseDto;
import com.graydang.app.domain.comment.model.dto.CommentResponseDto;
import com.graydang.app.domain.comment.model.dto.CommentSaveRequestDto;
import com.graydang.app.domain.comment.model.dto.CommentSimpleResponseDto;
import com.graydang.app.domain.comment.repository.CommentRepository;
import com.graydang.app.domain.comment.repository.projection.CommentSimpleProjection;
import com.graydang.app.domain.user.model.User;
import com.graydang.app.domain.user.service.UserService;
import com.graydang.app.global.common.model.dto.SliceResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentApplicationService {

    private final CommentRepository commentRepository;
    private final BillService billService;
    private final CommentService commentService;
    private final UserService userService;

    @Transactional
    public long createComment(CommentSaveRequestDto requestDto, long userId, long billId) {

        User user = userService.findByIdOrThrow(userId);

        Bill bill = billService.findByIdOrThrow(billId);

        Comment comment = requestDto.toEntity(user, bill);
        Comment savedComment = commentRepository.save(comment);
        return savedComment.getId();
    }

    public CommentReadResponseDto getCommentByBillId(CustomUserDetails userDetails, Long billId, Pageable pageable) {

        Bill bill = billService.findByIdOrThrow(billId);

        Slice<Comment> commentSlice = commentRepository.findByBillIdAndStatusOrderByCreatedAtDesc(bill.getId(), "ACTIVE", pageable);

        Set<Long> likedCommentIds = (userDetails != null) ? commentRepository.findCommentIdsByUserIdAndStatus(userDetails.getUser().getId(), "ACTIVE") : Set.of();

        List<CommentResponseDto> commentResponseDtoList = commentSlice.getContent().stream()
                .map(comment -> CommentMapper.toCommentResponseDto(
                        comment,
                        likedCommentIds.contains(comment.getId())
                ))
                .toList();

        SliceResponse<CommentResponseDto> sliceResponse = new SliceResponse<>(commentResponseDtoList, commentSlice.getNumber(), commentSlice.isLast());

        long totalCount = commentRepository.countByBill(bill);

        return new CommentReadResponseDto(totalCount, sliceResponse);
    }

    public SliceResponse<CommentSimpleResponseDto> getCommentInfoByUserId(CustomUserDetails userDetails, Pageable pageable) {

        Slice<CommentSimpleResponseDto> slice = commentRepository.findMyCommentsByUserId(userDetails.getUser().getId(), pageable)
                .map(CommentSimpleResponseDto::from);

        return new SliceResponse<>(slice);
    }
}
