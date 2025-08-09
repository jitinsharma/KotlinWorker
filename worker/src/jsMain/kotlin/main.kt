@file:OptIn(ExperimentalTime::class)

import com.jitinsharma.worker.models.AIPrompt
import com.jitinsharma.worker.models.AIRequest
import com.jitinsharma.worker.models.AIResponse
import com.jitinsharma.worker.models.AITask
import com.jitinsharma.worker.models.WorkerResponse
import com.jitinsharma.worker.models.toJson
import com.jitinsharma.worker.network.ConferenceService
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.promise
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mapper.ConferenceTransformer
import org.w3c.fetch.Request
import org.w3c.fetch.Response
import org.w3c.fetch.ResponseInit
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalJsExport::class, DelicateCoroutinesApi::class)
@JsExport
fun fetch(request: Request, env: dynamic = null, ctx: dynamic = null): dynamic {
    return GlobalScope.promise {
        if (request.method.contains("POST")) {
            val bodyText = request.text().await()
            val aiRequest = Json.decodeFromString<AIRequest>(bodyText)
            handleAIRequest(env, aiRequest)
        } else {
            handleRequest(request, env, ctx)
        }
    }
}

@OptIn(ExperimentalTime::class)
suspend fun handleRequest(request: Request, env: dynamic = null, ctx: dynamic = null): Response {
    val headers: dynamic = object {}
    headers["content-type"] = "application/json"
    headers["cache-control"] = "private, max-age=600"

    return try {
        val conferenceService = ConferenceService()

        // Fetch conferences using Ktor
        val rawConferences = conferenceService.getUpcomingConferences()

        // Transform conferences with epoch dates (worker-specific logic)
        val transformedConferences = ConferenceTransformer.transformConferences(rawConferences)
        val jsonResponse = Json.encodeToString(transformedConferences)

        Response(
            jsonResponse,
            ResponseInit(headers = headers)
        )

    } catch (e: Exception) {
        // Return error response
        val errorResponse = WorkerResponse(
            message = "Error fetching conferences: ${e.message}",
            timestamp = Clock.System.now().toEpochMilliseconds(),
            status = "error"
        )

        Response(
            errorResponse.toJson(),
            ResponseInit(headers = headers)
        )
    }
}

suspend fun handleAIRequest(env: dynamic, request: AIRequest): Response {
    val headers: dynamic = object {}
    headers["content-type"] = "application/json"

    return try {
        // Check if AI binding is available
        if (env == null || env.ai == undefined) {
            console.log("AI binding not available in env")
            return Response(
                Json.encodeToString(
                    WorkerResponse(
                        message = "AI binding not configured. Please add AI binding to wrangler.toml",
                        timestamp = Clock.System.now().toEpochMilliseconds(),
                        status = "error"
                    )
                ),
                ResponseInit(headers = headers, status = 500)
            )
        }

        // Create simple prompt using Kotlin data class
        val simple = AIPrompt(
            prompt = """
            Provide one paragraph information about ${request.url}
            Response should only contain text and should give brief about conference
            topics, speakers and location.
        """.trimIndent()
        )

        // Convert to dynamic object for the AI API call
        val promptObject = js("{}")
        promptObject.prompt = simple.prompt

        // Run AI model
        console.log("exec prompt: ${simple.prompt}")

        // Convert JavaScript Promise to Kotlin Promise and await it
        val aiPromise = env.ai.run("@cf/meta/llama-3-8b-instruct", promptObject)
        val aiResponse = aiPromise.unsafeCast<kotlin.js.Promise<dynamic>>().await()

        console.log("ai response object:", JSON.stringify(aiResponse))

        // Extract the response text from the AI response
        val responseText = if (aiResponse != null && aiResponse.response != undefined) {
            aiResponse.response.toString()
        } else {
            aiResponse.toString()
        }

        console.log("ai response text:", responseText)

        // Create task using Kotlin data class
        val task = AITask(
            inputs = simple,
            response = AIResponse(response = responseText)
        )

        // Serialize to JSON using kotlinx.serialization
        val jsonResponse = Json.encodeToString(task)
        console.log("json response: $aiResponse")

        Response(
            jsonResponse,
            ResponseInit(headers = headers)
        )

    } catch (e: Exception) {
        console.log("Error in AI request:", e.message)
        val errorResponse = WorkerResponse(
            message = "Error in AI request: ${e.message}",
            timestamp = Clock.System.now().toEpochMilliseconds(),
            status = "error"
        )

        Response(
            errorResponse.toJson(),
            ResponseInit(headers = headers, status = 500)
        )
    }
}