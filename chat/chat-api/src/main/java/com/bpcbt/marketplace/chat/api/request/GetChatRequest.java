package com.bpcbt.marketplace.chat.api.request;

import com.bpcbt.marketplace.boot.user.api.global.security.jwt.JwtSecurityUser;
import com.bpcbt.marketplace.boot.user.api.request.SecurityRequestModifier;
import com.bpcbt.marketplace.boot.user.api.validation.SecurityRequestModify;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@SecurityRequestModify
@Builder
public class GetChatRequest implements SecurityRequestModifier {

    private Long profileId;

    @Override
    public void modifyRequest(JwtSecurityUser principal) {
        this.profileId = principal.getCurrentProfileReferenceId();
    }
}
