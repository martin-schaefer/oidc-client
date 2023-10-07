package ch.martin.oidcclient;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;
import java.util.TreeMap;

@Controller
public class OidcRestController {

    private static final String PREFERRED_USERNAME = "preferredUsername";
    private static final String CLAIMS = "claims";
    private static final String INFOS = "infos";

    @GetMapping("/user")
    public String claims(Model model, @AuthenticationPrincipal OidcUser oidcUser) {
        OidcIdToken idToken = oidcUser.getIdToken();
        model.addAttribute(PREFERRED_USERNAME, idToken.getPreferredUsername());
        model.addAttribute(CLAIMS, new TreeMap<>(idToken.getClaims()));
        model.addAttribute(INFOS, new TreeMap<>(oidcUser.getUserInfo().getClaims()));
        return CLAIMS;
    }
}
