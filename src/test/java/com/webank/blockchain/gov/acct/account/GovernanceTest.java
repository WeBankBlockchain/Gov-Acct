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
package com.webank.blockchain.gov.acct.account;

import com.webank.blockchain.gov.acct.BaseTests;
import com.webank.blockchain.gov.acct.constant.AccountConstants;
import com.webank.blockchain.gov.acct.contract.WEGovernance;
import org.junit.jupiter.api.Test;

/**
 * GovernanceTest @Description: GovernanceTest
 *
 * @author maojiayu
 * @data Feb 3, 2020 5:14:31 PM
 */
public class GovernanceTest extends BaseTests {

    @Test
    public void test() throws Exception {
        // deploy
        WEGovernance governanceP1 =
                WEGovernance.deploy(client, p1, AccountConstants.VOTE_MODE)
                        ;
        System.out.println("Governance acct: " + governanceP1.getContractAddress());
        String acctManagerAddr = governanceP1.getAccountManager();
        System.out.println("acct manager addr: " + acctManagerAddr);
    }
}
