/*
 * Copyright (c) 2018 DuckDuckGo
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

package com.duckduckgo.app.fire

import com.duckduckgo.app.browser.cookies.CookieManagerProvider
import com.duckduckgo.app.global.AppUrl
import com.duckduckgo.app.global.DispatcherProvider
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

interface DuckDuckGoCookieManager {
    suspend fun removeExternalCookies()
    fun flush()
}

class WebViewCookieManager(
    private val cookieManager: CookieManagerProvider,
    private val removeCookies: RemoveCookiesStrategy,
    private val dispatcher: DispatcherProvider
) : DuckDuckGoCookieManager {

    override suspend fun removeExternalCookies() {
        withContext(dispatcher.io()) {
            flush()
        }
        // The Fire Button does not delete the user's DuckDuckGo search settings, which are saved as cookies.
        // Removing these cookies would reset them and have undesired consequences, i.e. changing the theme, default language, etc.
        // The Fire Button also does not delete temporary cookies associated with 'surveys.duckduckgo.com'.
        // When we launch surveys to help us understand issues that impact users over time, we use this cookie to temporarily store anonymous
        // survey answers, before deleting the cookie. Cookie storage duration is communicated to users before they opt to submit survey answers.
        // These cookies are not stored in a personally identifiable way. For example, the large size setting is stored as 's=l.'
        // More info in https://duckduckgo.com/privacy
        val ddgCookies = getDuckDuckGoCookies()
        if (cookieManager.get().hasCookies()) {
            removeCookies.removeCookies()
            storeDuckDuckGoCookies(ddgCookies)
        }
        withContext(dispatcher.io()) {
            flush()
        }
    }

    private suspend fun storeDuckDuckGoCookies(cookies: Map<String, List<String>>) {
        cookies.keys.forEach { host ->
            cookies[host]?.forEach {
                val cookie = it.trim()
                Timber.d("Restoring DDB cookie: $cookie")
                storeCookie(cookie, host)
            }
        }
    }

    private suspend fun storeCookie(cookie: String, host: String) {
        suspendCoroutine { continuation ->
            cookieManager.get().setCookie(host, cookie) { success ->
                Timber.v("Cookie $cookie stored successfully: $success")
                continuation.resume(Unit)
            }
        }
    }

    private fun getDuckDuckGoCookies(): Map<String, List<String>> {
        val map = mutableMapOf<String, List<String>>()
        DDG_COOKIE_DOMAINS.forEach { host ->
            map[host] = cookieManager.get().getCookie(host)?.split(";").orEmpty()
        }
        return map
    }

    override fun flush() {
        cookieManager.get().flush()
    }

    companion object {
        private val DDG_COOKIE_DOMAINS = listOf(AppUrl.Url.COOKIES, AppUrl.Url.SURVEY_COOKIES)
    }

}
