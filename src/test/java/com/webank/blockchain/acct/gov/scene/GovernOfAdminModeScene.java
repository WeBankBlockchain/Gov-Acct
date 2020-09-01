/**
 * Copyright 2014-2019 the original author or authors.
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.blockchain.acct.gov.scene;

import com.webank.blockchain.acct.gov.BaseTests;
import com.webank.blockchain.acct.gov.contract.AccountManager;
import com.webank.blockchain.acct.gov.contract.WEGovernance;
import com.webank.blockchain.acct.gov.manager.AdminModeGovernManager;
import com.webank.blockchain.acct.gov.manager.GovernAccountInitializer;
import com.webank.blockchain.acct.gov.service.BaseAccountService;
import org.fisco.bcos.web3j.protocol.channel.StatusCode;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * GovernOfAdminModeScene @Description: GovernOfAdminModeScene
 *
 * @author maojiayu
 * @data Feb 22, 2020 3:11:09 PM
 */
public class GovernOfAdminModeScene extends BaseTests {
    @Autowired private GovernAccountInitializer manager;
    @Autowired private AdminModeGovernManager adminModeManager;
    @Autowired private AccountManager accountManager;
    @Autowired private BaseAccountService baseAccountService;
    @Autowired private WEGovernance govern;

    // @Test
    // create govern account of admin by user, and set the address in application.properties
    public void testCreate() throws Exception {
        WEGovernance govern = manager.createGovernAccount(u);
        System.out.println(govern.getContractAddress());
        Assertions.assertNotNull(govern);
    }

    @Test
    public void testAdminScene() throws Exception {
        // set in super admin mode.
        adminModeManager.setCredentials(u);
        Assertions.assertEquals(0, govern._mode().send().intValue());

        // create account by admin
        if (!accountManager.hasAccount(u1.getAddress()).send()) {
            adminModeManager.createAccount(u1.getAddress());
        }
        String u1Address = adminModeManager.getBaseAccountAddress(u1.getAddress());
        Assertions.assertNotNull(u1Address);
        String u1AccountAddress = accountManager.getUserAccount(u1.getAddress()).send();
        Assertions.assertEquals(u1AccountAddress, u1Address);

        // reset acct
        Assertions.assertEquals(govern.getContractAddress(), accountManager._owner().send());
        TransactionReceipt tr = adminModeManager.resetAccount(u1.getAddress(), u2.getAddress());
        Assertions.assertEquals(StatusCode.Success, tr.getStatus());
        Assertions.assertTrue(!accountManager.hasAccount(u1.getAddress()).send());
        Assertions.assertTrue(accountManager.hasAccount(u2.getAddress()).send());

        // set back again
        tr = adminModeManager.resetAccount(u2.getAddress(), u1.getAddress());
        Assertions.assertEquals("0x0", tr.getStatus());

        // freeze
        tr = adminModeManager.freezeAccount(u1.getAddress());
        Assertions.assertEquals("0x0", tr.getStatus());
        Assertions.assertEquals(1, baseAccountService.getStatus(u1, u1AccountAddress));

        // transfer to abnormal
        tr = adminModeManager.transferAdminAuth(u1.getAddress());
        Assertions.assertNotEquals("0x0", tr.getStatus());

        // transfer to not registered
        tr = adminModeManager.transferAdminAuth(p1.getAddress());
        Assertions.assertNotEquals("0x0", tr.getStatus());

        // unfreeze
        tr = adminModeManager.unfreezeAccount(u1.getAddress());
        Assertions.assertEquals("0x0", tr.getStatus());
        Assertions.assertEquals(0, baseAccountService.getStatus(u1, u1AccountAddress));

        // cancel
        tr = adminModeManager.cancelAccount(u1.getAddress());
        Assertions.assertEquals("0x0", tr.getStatus());
        Assertions.assertEquals(2, baseAccountService.getStatus(u1, u1AccountAddress));
        Assertions.assertTrue(!accountManager.hasAccount(u1.getAddress()).send());

        // create again
        Assertions.assertTrue(!accountManager.hasAccount(u1.getAddress()).send());
        String newAcct = adminModeManager.createAccount(u1.getAddress());
        Assertions.assertNotNull(newAcct);
        Assertions.assertTrue(accountManager.hasAccount(u1.getAddress()).send());

        // cancel
        tr = adminModeManager.cancelAccount(u1.getAddress());
        Assertions.assertEquals("0x0", tr.getStatus());
        Assertions.assertEquals(2, baseAccountService.getStatus(u1, u1AccountAddress));
        Assertions.assertTrue(!accountManager.hasAccount(u1.getAddress()).send());

        // transfer
        adminModeManager.createAccount(u2.getAddress());
        tr = adminModeManager.transferAdminAuth(u2.getAddress());
        Assertions.assertEquals("0x0", tr.getStatus());
        String owner = govern._owner().send();
        Assertions.assertEquals(u2.getAddress(), owner);
    }
}