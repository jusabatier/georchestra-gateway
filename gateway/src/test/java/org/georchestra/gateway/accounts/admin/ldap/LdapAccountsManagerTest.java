package org.georchestra.gateway.accounts.admin.ldap;

import org.georchestra.ds.DataServiceException;
import org.georchestra.ds.roles.RoleDao;
import org.georchestra.ds.users.AccountDao;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.ldap.NameNotFoundException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LdapAccountsManagerTest {

    public @Test void testEnsureRoleExist() throws DataServiceException {
        RoleDao roleDao = mock(RoleDao.class);
        when(roleDao.findByCommonName(anyString())).thenThrow(new NameNotFoundException("FAKE_ROLE"));

        LdapAccountsManager toTest = new LdapAccountsManager(mock(ApplicationEventPublisher.class), null, roleDao, null,
                null, null, null);

        toTest.ensureRoleExists("FAKE_ROLE");
        // No exception thrown
    }
}
