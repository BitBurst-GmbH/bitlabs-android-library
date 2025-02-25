package ai.bitlabs.sdk.data.model.sentry

internal class SentryDsn(private val dsn: String) {
    val host: String
    val protocol: String
    val publicKey: String
    val projectId: String

    init {
        val regex = Regex("(\\w+)://(\\w+)@(.*)/(\\w+)")
        val match = regex.find(dsn) ?: throw IllegalArgumentException("Invalid DSN: $dsn")
        val (protocol, publicKey, host, projectId) = match.destructured

        this.host = host
        this.protocol = protocol
        this.publicKey = publicKey
        this.projectId = projectId
    }

    override fun toString(): String = dsn
}