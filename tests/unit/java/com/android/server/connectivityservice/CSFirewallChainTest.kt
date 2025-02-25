/*
 * Copyright (C) 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.server

import android.net.BpfNetMapsConstants.METERED_ALLOW_CHAINS
import android.net.BpfNetMapsConstants.METERED_DENY_CHAINS
import android.net.ConnectivityManager.FIREWALL_CHAIN_BACKGROUND
import android.net.ConnectivityManager.FIREWALL_CHAIN_METERED_ALLOW
import android.net.ConnectivityManager.FIREWALL_CHAIN_METERED_DENY_USER
import android.net.ConnectivityManager.FIREWALL_RULE_ALLOW
import android.net.ConnectivityManager.FIREWALL_RULE_DEFAULT
import android.net.ConnectivityManager.FIREWALL_RULE_DENY
import android.os.Build
import androidx.test.filters.SmallTest
import com.android.server.connectivity.ConnectivityFlags.BACKGROUND_FIREWALL_CHAIN
import com.android.testutils.DevSdkIgnoreRule
import com.android.testutils.DevSdkIgnoreRule.IgnoreAfter
import com.android.testutils.DevSdkIgnoreRule.IgnoreUpTo
import com.android.testutils.DevSdkIgnoreRunner
import com.android.testutils.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito.any
import org.mockito.Mockito.clearInvocations
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

@RunWith(DevSdkIgnoreRunner::class)
@SmallTest
@IgnoreUpTo(Build.VERSION_CODES.S_V2)
class CSFirewallChainTest : CSTest() {
    @get:Rule
    val ignoreRule = DevSdkIgnoreRule()

    // Tests for setFirewallChainEnabled on FIREWALL_CHAIN_BACKGROUND
    @Test
    @FeatureFlags(flags = [Flag(BACKGROUND_FIREWALL_CHAIN, false)])
    fun setFirewallChainEnabled_backgroundChainDisabled() {
        verifySetFirewallChainEnabledOnBackgroundDoesNothing()
    }

    @Test
    @FeatureFlags(flags = [Flag(BACKGROUND_FIREWALL_CHAIN, true)])
    @IgnoreUpTo(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    fun setFirewallChainEnabled_backgroundChainEnabled_afterU() {
        cm.setFirewallChainEnabled(FIREWALL_CHAIN_BACKGROUND, true)
        verify(bpfNetMaps).setChildChain(FIREWALL_CHAIN_BACKGROUND, true)

        clearInvocations(bpfNetMaps)

        cm.setFirewallChainEnabled(FIREWALL_CHAIN_BACKGROUND, false)
        verify(bpfNetMaps).setChildChain(FIREWALL_CHAIN_BACKGROUND, false)
    }

    @Test
    @FeatureFlags(flags = [Flag(BACKGROUND_FIREWALL_CHAIN, true)])
    @IgnoreAfter(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    fun setFirewallChainEnabled_backgroundChainEnabled_uptoU() {
        verifySetFirewallChainEnabledOnBackgroundDoesNothing()
    }

    private fun verifySetFirewallChainEnabledOnBackgroundDoesNothing() {
        cm.setFirewallChainEnabled(FIREWALL_CHAIN_BACKGROUND, true)
        verify(bpfNetMaps, never()).setChildChain(anyInt(), anyBoolean())

        cm.setFirewallChainEnabled(FIREWALL_CHAIN_BACKGROUND, false)
        verify(bpfNetMaps, never()).setChildChain(anyInt(), anyBoolean())
    }

    // Tests for replaceFirewallChain on FIREWALL_CHAIN_BACKGROUND
    @Test
    @FeatureFlags(flags = [Flag(BACKGROUND_FIREWALL_CHAIN, false)])
    fun replaceFirewallChain_backgroundChainDisabled() {
        verifyReplaceFirewallChainOnBackgroundDoesNothing()
    }

    @Test
    @FeatureFlags(flags = [Flag(BACKGROUND_FIREWALL_CHAIN, true)])
    @IgnoreUpTo(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    fun replaceFirewallChain_backgroundChainEnabled_afterU() {
        val uids = intArrayOf(53, 42, 79)
        cm.replaceFirewallChain(FIREWALL_CHAIN_BACKGROUND, uids)
        verify(bpfNetMaps).replaceUidChain(FIREWALL_CHAIN_BACKGROUND, uids)
    }

    @Test
    @FeatureFlags(flags = [Flag(BACKGROUND_FIREWALL_CHAIN, true)])
    @IgnoreAfter(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    fun replaceFirewallChain_backgroundChainEnabled_uptoU() {
        verifyReplaceFirewallChainOnBackgroundDoesNothing()
    }

    private fun verifyReplaceFirewallChainOnBackgroundDoesNothing() {
        val uids = intArrayOf(53, 42, 79)
        cm.replaceFirewallChain(FIREWALL_CHAIN_BACKGROUND, uids)
        verify(bpfNetMaps, never()).replaceUidChain(anyInt(), any(IntArray::class.java))
    }

    // Tests for setUidFirewallRule on FIREWALL_CHAIN_BACKGROUND
    @Test
    @FeatureFlags(flags = [Flag(BACKGROUND_FIREWALL_CHAIN, false)])
    fun setUidFirewallRule_backgroundChainDisabled() {
        verifySetUidFirewallRuleOnBackgroundDoesNothing()
    }

    @Test
    @FeatureFlags(flags = [Flag(BACKGROUND_FIREWALL_CHAIN, true)])
    @IgnoreUpTo(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    fun setUidFirewallRule_backgroundChainEnabled_afterU() {
        val uid = 2345

        cm.setUidFirewallRule(FIREWALL_CHAIN_BACKGROUND, uid, FIREWALL_RULE_DEFAULT)
        verify(bpfNetMaps).setUidRule(FIREWALL_CHAIN_BACKGROUND, uid, FIREWALL_RULE_DENY)

        clearInvocations(bpfNetMaps)

        cm.setUidFirewallRule(FIREWALL_CHAIN_BACKGROUND, uid, FIREWALL_RULE_DENY)
        verify(bpfNetMaps).setUidRule(FIREWALL_CHAIN_BACKGROUND, uid, FIREWALL_RULE_DENY)

        clearInvocations(bpfNetMaps)

        cm.setUidFirewallRule(FIREWALL_CHAIN_BACKGROUND, uid, FIREWALL_RULE_ALLOW)
        verify(bpfNetMaps).setUidRule(FIREWALL_CHAIN_BACKGROUND, uid, FIREWALL_RULE_ALLOW)
    }

    @Test
    @FeatureFlags(flags = [Flag(BACKGROUND_FIREWALL_CHAIN, true)])
    @IgnoreAfter(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    fun setUidFirewallRule_backgroundChainEnabled_uptoU() {
        verifySetUidFirewallRuleOnBackgroundDoesNothing()
    }

    private fun verifySetUidFirewallRuleOnBackgroundDoesNothing() {
        val uid = 2345

        listOf(FIREWALL_RULE_DEFAULT, FIREWALL_RULE_ALLOW, FIREWALL_RULE_DENY).forEach { rule ->
            cm.setUidFirewallRule(FIREWALL_CHAIN_BACKGROUND, uid, rule)
            verify(bpfNetMaps, never()).setUidRule(anyInt(), anyInt(), anyInt())
        }
    }

    @Test
    fun testSetFirewallChainEnabled_meteredChain() {
        (METERED_ALLOW_CHAINS + METERED_DENY_CHAINS).forEach {
            assertThrows(UnsupportedOperationException::class.java) {
                cm.setFirewallChainEnabled(it, true)
            }
            assertThrows(UnsupportedOperationException::class.java) {
                cm.setFirewallChainEnabled(it, false)
            }
        }
    }

    @Test
    fun testAddUidToMeteredNetworkAllowList() {
        val uid = 1001
        cm.addUidToMeteredNetworkAllowList(uid)
        verify(bpfNetMaps).setUidRule(FIREWALL_CHAIN_METERED_ALLOW, uid, FIREWALL_RULE_ALLOW)
    }

    @Test
    fun testRemoveUidFromMeteredNetworkAllowList() {
        val uid = 1001
        cm.removeUidFromMeteredNetworkAllowList(uid)
        verify(bpfNetMaps).setUidRule(FIREWALL_CHAIN_METERED_ALLOW, uid, FIREWALL_RULE_DENY)
    }

    @Test
    fun testAddUidToMeteredNetworkDenyList() {
        val uid = 1001
        cm.addUidToMeteredNetworkDenyList(uid)
        verify(bpfNetMaps).setUidRule(FIREWALL_CHAIN_METERED_DENY_USER, uid, FIREWALL_RULE_DENY)
    }

    @Test
    fun testRemoveUidFromMeteredNetworkDenyList() {
        val uid = 1001
        cm.removeUidFromMeteredNetworkDenyList(uid)
        verify(bpfNetMaps).setUidRule(FIREWALL_CHAIN_METERED_DENY_USER, uid, FIREWALL_RULE_ALLOW)
    }
}
