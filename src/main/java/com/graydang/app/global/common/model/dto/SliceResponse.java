package com.graydang.app.global.common.model.dto;

import lombok.Getter;
import org.springframework.data.domain.Slice;

import java.util.List;

@Getter
public class SliceResponse<T> {

    List<T> content;
    private int pageNumber;
    private boolean isLast;

    public SliceResponse(Slice<T> content) {
        this.content = content.getContent();
        this.pageNumber = content.getNumber();
        this.isLast = content.isLast();
    }

    public SliceResponse(Slice<T> content, int pageNumber, boolean isLast) {
        this.content = content.getContent();
        this.pageNumber = pageNumber;
        this.isLast = isLast;
    }


}
