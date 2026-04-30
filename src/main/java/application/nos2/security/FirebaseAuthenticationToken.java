package application.nos2.security;

import com.google.firebase.auth.FirebaseToken;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;

public final class FirebaseAuthenticationToken extends AbstractAuthenticationToken {

    private final FirebaseToken principal;

    public FirebaseAuthenticationToken(FirebaseToken principal) {
        super(AuthorityUtils.NO_AUTHORITIES);
        this.principal = principal;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return "";
    }

    @Override
    public FirebaseToken getPrincipal() {
        return principal;
    }
}
