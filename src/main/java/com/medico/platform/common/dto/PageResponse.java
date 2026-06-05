package com.medico.platform.common.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * envelope de paginacao estavel para a API.
 *
 * <p>Evita expor o {@code org.springframework.data.domain.PageImpl} diretamente
 * (cujo contrato JSON nao e garantido entre versoes), entregando um formato
 * limpo e versionavel ao cliente.</p>
 *
 * @param <T> tipo do conteudo paginado
 */
@Getter
public class PageResponse<T> {

    private final List<T> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final boolean first;
    private final boolean last;

    @JsonCreator
    public PageResponse(
            @JsonProperty("content") List<T> content,
            @JsonProperty("page") int page,
            @JsonProperty("size") int size,
            @JsonProperty("totalElements") long totalElements,
            @JsonProperty("totalPages") int totalPages,
            @JsonProperty("first") boolean first,
            @JsonProperty("last") boolean last) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.first = first;
        this.last = last;
    }

    /** converte uma {@link Page} do Spring Data no envelope da API. */
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast());
    }
}
