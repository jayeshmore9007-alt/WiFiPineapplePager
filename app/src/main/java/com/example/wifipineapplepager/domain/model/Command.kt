package com.example.wifipineapplepager.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class WifiPineappleCommand(
    val endpoint: String,
    val allowedParams: Set<String>,
    val requiresApproval: Boolean = false
) {
    STATUS("/api/status", setOf()),
    SCAN("/api/scan", setOf("duration", "channel")),
    REBOOT("/api/reboot", setOf("delay_seconds")),
    STOP_SCAN("/api/scan/stop", setOf()),
    GET_NETWORKS("/api/networks", setOf()),
    GET_LOGS("/api/logs", setOf("lines", "filter"));

fun isAllowedParam(param: String): Boolean = allowedParams.contains(param)
}

@Serializable
data class AgentDecision(
    val command: String,
    val params: Map<String, String> = emptyMap(),
    val reason: String = "",
    val requiresApproval: Boolean = false
)
