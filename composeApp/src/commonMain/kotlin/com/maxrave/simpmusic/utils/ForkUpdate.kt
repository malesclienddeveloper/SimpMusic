package com.maxrave.simpmusic.utils

import com.maxrave.domain.data.model.update.UpdateData
import com.maxrave.domain.utils.Resource
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Where THIS fork publishes its releases.
 *
 * [GITHUB_REPOSITORY] is a placeholder that the release workflow rewrites to `owner/repo` of the
 * repository it runs in, just before building. In a local build it stays unresolved and in-app
 * updates are simply switched off — nothing here ever talks to the upstream project.
 */
object ForkConfig {
    const val GITHUB_REPOSITORY = "__FORK_GITHUB_REPOSITORY__"

    val isConfigured: Boolean
        get() = GITHUB_REPOSITORY.contains('/') && !GITHUB_REPOSITORY.startsWith("__")

    val releasesPageUrl: String
        get() = if (isConfigured) "https://github.com/$GITHUB_REPOSITORY/releases/latest" else ""
}

/**
 * In-app update check against the fork's own GitHub releases.
 *
 * It hands back the same [UpdateData] the upstream check does, so the existing update dialog and
 * its "is this newer than me" comparison keep working unchanged. A release tag is expected to be
 * the app's version name with an optional leading "v" (`v1.2.0`).
 */
object ForkUpdateChecker {
    private val json = Json { ignoreUnknownKeys = true }

    fun check(): Flow<Resource<UpdateData>> =
        flow {
            if (!ForkConfig.isConfigured) {
                emit(Resource.Error<UpdateData>("In-app updates are not configured for this build"))
                return@flow
            }
            val client = HttpClient(CIO)
            try {
                val response =
                    client.get("https://api.github.com/repos/${ForkConfig.GITHUB_REPOSITORY}/releases/latest") {
                        header("Accept", "application/vnd.github+json")
                    }
                if (!response.status.isSuccess()) {
                    emit(Resource.Error<UpdateData>("Update check failed: HTTP ${response.status.value}"))
                    return@flow
                }
                val release: JsonObject = json.parseToJsonElement(response.bodyAsText()).jsonObject
                val tag = release["tag_name"]?.jsonPrimitive?.contentOrNull
                if (tag.isNullOrBlank()) {
                    emit(Resource.Error<UpdateData>("Update check failed: release has no tag"))
                } else {
                    emit(
                        Resource.Success(
                            UpdateData(
                                tagName = tag,
                                releaseTime = release["published_at"]?.jsonPrimitive?.contentOrNull,
                                body = release["body"]?.jsonPrimitive?.contentOrNull ?: "",
                            ),
                        ),
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                emit(Resource.Error<UpdateData>(e.message ?: "Unknown error"))
            } finally {
                client.close()
            }
        }.flowOn(Dispatchers.IO)
}
