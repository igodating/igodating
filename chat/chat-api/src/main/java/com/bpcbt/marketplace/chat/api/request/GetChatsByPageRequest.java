package com.bpcbt.marketplace.chat.api.request;

import com.bpcbt.marketplace.boot.user.api.global.security.jwt.JwtSecurityUser;
import com.bpcbt.marketplace.boot.user.api.request.SecurityRequestModifier;
import com.bpcbt.marketplace.boot.user.api.validation.SecurityRequestModify;
import com.bpcbt.marketplace.chat.api.ChatType;
import com.bpcbt.marketplace.commons.common.request.PaginationRequest;
import com.bpcbt.marketplace.commons.global.ProfileType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@SecurityRequestModify
public class GetChatsByPageRequest implements SecurityRequestModifier {

    @NotNull
    @Valid
    private PaginationRequest paginationRequest;
    private ChatType type;
    private Long profileId;
    private ProfileType profileType;
    @Min(0)
    private long messageLimit;

    @Override
    public void modifyRequest(JwtSecurityUser principal) {
        profileType = principal.getCurrentProfile();
        profileId = principal.getCurrentProfileReferenceId();
    }
}
