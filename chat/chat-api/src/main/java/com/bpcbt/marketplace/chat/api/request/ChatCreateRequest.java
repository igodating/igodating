package com.bpcbt.marketplace.chat.api.request;

import com.bpcbt.marketplace.boot.user.api.global.security.jwt.JwtSecurityUser;
import com.bpcbt.marketplace.boot.user.api.request.SecurityRequestModifier;
import com.bpcbt.marketplace.boot.user.api.validation.SecurityRequestModify;
import com.bpcbt.marketplace.chat.api.ChatType;
import com.bpcbt.marketplace.commons.global.ProfileType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@SecurityRequestModify
@Builder
public class ChatCreateRequest implements SecurityRequestModifier {

    @NotNull
    private String title;
    @NotNull
    private ChatType type;
    private Long entityId;
    private Long creatorProfileId;
    private ProfileType creatorProfileType;
    private Set<Opponent> opponents;

    @Override
    public void modifyRequest(JwtSecurityUser principal) {
        this.creatorProfileId = principal.getCurrentProfileReferenceId();
        this.creatorProfileType = principal.getCurrentProfile();
    }

    @AllArgsConstructor
    @Getter
    @Setter
    public static class Opponent {
        private long profileId;
        private boolean legal;
    }
}
