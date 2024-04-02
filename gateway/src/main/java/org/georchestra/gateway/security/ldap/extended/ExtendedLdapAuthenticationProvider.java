package org.georchestra.gateway.security.ldap.extended;

import org.georchestra.ds.DataServiceException;
import org.georchestra.ds.users.Account;
import org.georchestra.ds.users.AccountDao;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.authentication.LdapAuthenticator;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class ExtendedLdapAuthenticationProvider extends LdapAuthenticationProvider {

    private AccountDao accountDao;

    public ExtendedLdapAuthenticationProvider(LdapAuthenticator authenticator,
            LdapAuthoritiesPopulator authoritiesPopulator) {
        super(authenticator, authoritiesPopulator);
    }

    public void setAccountDao(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Assert.isInstanceOf(UsernamePasswordAuthenticationToken.class, authentication,
                () -> this.messages.getMessage("LdapAuthenticationProvider.onlySupports",
                        "Only UsernamePasswordAuthenticationToken is supported"));
        UsernamePasswordAuthenticationToken userToken = (UsernamePasswordAuthenticationToken) authentication;
        Account account = null;
        try {
            account = accountDao.findByEmail(userToken.getName());
        } catch (DataServiceException e) {
        } catch (NameNotFoundException e) {
        }
        if (account != null) {
            userToken = new UsernamePasswordAuthenticationToken(account.getUid(), userToken.getCredentials());
        }

        String username = userToken.getName();
        String password = (String) authentication.getCredentials();

        if (!StringUtils.hasLength(username)) {
            throw new BadCredentialsException(
                    this.messages.getMessage("LdapAuthenticationProvider.emptyUsername", "Empty Username"));
        }
        if (!StringUtils.hasLength(password)) {
            throw new BadCredentialsException(
                    this.messages.getMessage("AbstractLdapAuthenticationProvider.emptyPassword", "Empty Password"));
        }
        Assert.notNull(password, "Null password was supplied in authentication token");
        DirContextOperations userData = doAuthentication(userToken);
        UserDetails user = this.userDetailsContextMapper.mapUserFromContext(userData, username,
                loadUserAuthorities(userData, authentication.getName(), (String) authentication.getCredentials()));

        return createSuccessfulAuthentication(userToken, user);
    }
}
