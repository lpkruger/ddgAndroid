/*
 * Copyright (c) 2023 DuckDuckGo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duckduckgo.networkprotection.impl.fakes

import com.duckduckgo.networkprotection.impl.waitlist.store.NetPWaitlistDataStore

class FakeNetPWaitlistDataStore(
    override var settingUnlocked: Boolean = false,
    override var authToken: String? = null,
    override var waitlistToken: String? = null,
    override var waitlistTimestamp: Int = -1,
    override var didAcceptedTerms: Boolean = false,
) : NetPWaitlistDataStore {
    override fun clear() {
        settingUnlocked = false
        authToken = null
        waitlistToken = null
        waitlistTimestamp = -1
        didAcceptedTerms = false
    }
}
