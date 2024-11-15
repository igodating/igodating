package com.igodating.questionary.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TextEmbeddingResponseItem(
        @JsonProperty("sentence_id")
        String sentenceId,
        float[] embedding
) {
}
