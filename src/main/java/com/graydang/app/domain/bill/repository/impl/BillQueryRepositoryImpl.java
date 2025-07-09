package com.graydang.app.domain.bill.repository.impl;

import com.graydang.app.domain.bill.model.*;
import com.graydang.app.domain.bill.model.dto.BillSimpleResponseDto;
import com.graydang.app.domain.bill.repository.BillQueryRepository;
import com.graydang.app.domain.bill.repository.projection.BillSimpleProjection;
import com.graydang.app.domain.comment.model.QComment;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringTemplate;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class BillQueryRepositoryImpl implements BillQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<BillSimpleResponseDto> findBillSimpleProjectionByCommittees(Set<String> committeeLabels, Long userId, Pageable pageable, String sortBy) {
        QBill bill = QBill.bill;
        QBillStatusHistory history = QBillStatusHistory.billStatusHistory;
        QBillReaction reaction = QBillReaction.billReaction;
        QComment comment = QComment.comment;
        QBillScrape scrape = QBillScrape.billScrape;

        Expression<Boolean> scrapedExpr = (userId == null)
                ? Expressions.constant(false)
                : JPAExpressions.selectOne()
                    .from(scrape)
                    .where(
                            scrape.bill.eq(bill),
                            scrape.user.id.eq(userId),
                            scrape.status.eq("ACTIVE")
                    )
                    .exists();

        Expression<String> billHistoryStatusExpr = Expressions.stringTemplate(
                "coalesce({0}, {1})",
                JPAExpressions.select(history.stepName)
                        .from(history)
                        .where(
                                history.bill.eq(bill),
                                history.status.eq("ACTIVE"),
                                history.stepOrder.eq(
                                        JPAExpressions.select(history.stepOrder.max())
                                                .from(history)
                                                .where(
                                                        history.bill.eq(bill),
                                                        history.status.eq("ACTIVE")
                                                )
                                )
                        ),
                Expressions.constant("발의")
        );

        List<BillSimpleResponseDto> content =  queryFactory
                .select(Projections.constructor(
                        BillSimpleResponseDto.class,
                        bill.id.as("billId"),
                        bill.aiTitle,
                        bill.representativeName,
                        bill.proposeDate.stringValue().as("proposeDate"),
//                        ExpressionUtils.as(
//                                JPAExpressions.select(history.stepName)
//                                    .from(history)
//                                    .where(history.bill.eq(bill), history.status.eq("ACTIVE"))
//                                    .orderBy(history.stepOrder.desc())
//                                    .limit(1),
//                                "billHistoryStatus"
//                        ),
                        ExpressionUtils.as(
                                billHistoryStatusExpr,
                                "billHistoryStatus"
                        ),
                        bill.committeeName,
                        bill.viewCount,
                        ExpressionUtils.as(
                                JPAExpressions.select(reaction.count())
                                        .from(reaction)
                                        .where(reaction.bill.eq(bill), reaction.status.eq("ACTIVE")),
                                "reactionCount"
                        ),
                        ExpressionUtils.as(
                                JPAExpressions.select(comment.count())
                                        .from(comment)
                                        .where(comment.bill.eq(bill), comment.status.eq("ACTIVE")),
                                "commentCount"
                        ),
                        ExpressionUtils.as(scrapedExpr, "scraped")
                ))
                .from(bill)
                .where(bill.committeeName.in(committeeLabels), bill.status.eq("ACTIVE"))
                .orderBy(getSortOrder(sortBy))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = content.size() > pageable.getPageSize();
        if(hasNext) {
            content.remove(content.size() - 1);
        }

        return new SliceImpl<>(content, pageable, hasNext);
    }

    private OrderSpecifier<?>[] getSortOrder(String sortBy) {
        QBill bill = QBill.bill;
        if("proposeDate".equalsIgnoreCase(sortBy)) {
            return new OrderSpecifier[] {
                    bill.proposeDate.desc(),
                    bill.id.desc()
            };
        }
        return new OrderSpecifier[] {
                bill.viewCount.desc(),
                bill.id.desc()
        };
    }

    @Override
    public Slice<BillSimpleResponseDto> findBySearchKeyword(String keyword, Long userId, Pageable pageable, String sortBy) {
        QBill bill = QBill.bill;
        QBillStatusHistory history = QBillStatusHistory.billStatusHistory;
        QBillReaction reaction = QBillReaction.billReaction;
        QComment comment = QComment.comment;
        QBillScrape scrape = QBillScrape.billScrape;

        Expression<String> billHistoryStatusExpr = Expressions.stringTemplate(
                "coalesce({0}, {1})",
                JPAExpressions.select(history.stepName)
                        .from(history)
                        .where(
                                history.bill.eq(bill),
                                history.status.eq("ACTIVE"),
                                history.stepOrder.eq(
                                        JPAExpressions.select(history.stepOrder.max())
                                                .from(history)
                                                .where(
                                                        history.bill.eq(bill),
                                                        history.status.eq("ACTIVE")
                                                )
                                )
                        ),
                Expressions.constant("발의")
        );

        Expression<Boolean> scrapedExpr = (userId == null)
                ? Expressions.constant(false)
                : JPAExpressions.selectOne()
                .from(scrape)
                .where(
                        scrape.bill.eq(bill),
                        scrape.user.id.eq(userId),
                        scrape.status.eq("ACTIVE")
                )
                .exists();

        List<BillSimpleResponseDto> content = queryFactory
                .select(Projections.constructor(
                        BillSimpleResponseDto.class,
                        bill.id.as("billId"),
                        bill.aiTitle,
                        bill.representativeName,
                        bill.proposeDate.stringValue().as("proposeDate"),
                        ExpressionUtils.as(billHistoryStatusExpr, "billHistoryStatus"),
                        bill.committeeName,
                        bill.viewCount,
                        ExpressionUtils.as(
                                JPAExpressions.select(reaction.count())
                                        .from(reaction)
                                        .where(reaction.bill.eq(bill), reaction.status.eq("ACTIVE")),
                                "reactionCount"
                        ),
                        ExpressionUtils.as(
                                JPAExpressions.select(comment.count())
                                        .from(comment)
                                        .where(comment.bill.eq(bill), comment.status.eq("ACTIVE")),
                                "commentCount"
                        ),
                        ExpressionUtils.as(scrapedExpr, "scraped")
                ))
                .from(bill)
                .where(
                        bill.status.eq("ACTIVE"),
                        bill.aiTitle.containsIgnoreCase(keyword)
                )
                .orderBy(getSortOrder(sortBy))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = content.size() > pageable.getPageSize();
        if (hasNext) {
            content.remove(content.size() - 1);
        }

        return new SliceImpl<>(content, pageable, hasNext);
    }
}
