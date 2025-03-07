package com.igodating.commons.model.page;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;


@Getter
@Setter
@Accessors(chain = true)
public class Page<T> implements Serializable {

    protected final long totalCount;
    protected List<T> data;
    protected int currentPage;

    @JsonCreator
    public Page(@JsonProperty("data") List<T> data, @JsonProperty("totalCount") long totalCount, @JsonProperty("currentPage") int currentPage) {
        this(data, totalCount);
        this.currentPage = currentPage;
    }

    public Page(List<T> data, long totalCount) {
        this.data = data;
        this.totalCount = totalCount;
    }

    public static <V> Page<V> empty() {
        return new Page<>(Collections.emptyList(), 0L, 0);
    }

    @JsonIgnore
    public boolean isEmpty() {
        return totalCount == 0;
    }

    @JsonIgnore
    public boolean isEmptyData() {
        return CollectionUtils.isEmpty(data);
    }

    @JsonIgnore
    public T singleResult() {
        if (data.size() == 1) {
            return data.getFirst();
        }
        throw new IllegalStateException("Size more than 1");
    }

    public <V> Page<V> convertPage(@NotNull Function<T, V> converter) {
        List<V> convertData = convertList(data, converter);
        return new Page<>(convertData, this.totalCount, this.currentPage);
    }

    public <V> Page<V> convertWithCurrentPage(@NotNull Function<T, V> converter) {
        return this.convertPage(converter);
    }

    private <V> List<V> convertList(@NotNull List<T> list, @NotNull Function<T, V> converter) {
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list.stream()
                .map(converter)
                .collect(Collectors.toList());
    }

}
