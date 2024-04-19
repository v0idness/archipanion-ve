package org.vitrivr.engine.core.model.content.factory

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.vitrivr.engine.core.model.content.element.*
import org.vitrivr.engine.core.model.content.impl.cache.CachedAudioContent
import org.vitrivr.engine.core.model.content.impl.cache.CachedContent
import org.vitrivr.engine.core.model.content.impl.cache.CachedImageContent
import org.vitrivr.engine.core.model.content.impl.cache.CachedTextContent
import org.vitrivr.engine.core.model.mesh.Model3D
import java.awt.image.BufferedImage
import java.io.IOException
import java.lang.ref.PhantomReference
import java.lang.ref.ReferenceQueue
import java.nio.ShortBuffer
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

private val logger: KLogger = KotlinLogging.logger {}

/**
 * A [ContentFactory] that uses a file cache to back [ContentElement]s.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class CachedContentFactory : ContentFactory, AutoCloseable {

    /** The [ReferenceQueue] used for cleanup. */
    private val referenceQueue = ReferenceQueue<CachedContent>()

    /** Set of all the [CachedItem]s. */
    private val refSet = HashSet<CachedItem>()

    /** Internal counter used. */
    private val counter = AtomicInteger(0)

    /** The path to the file cache. */
    private val cachePath: Path = Paths.get("./cache")

    /** Flag indicating, that this [CachedContentFactory] has been closed. */
    @Volatile
    var closed = false
        private set

    init {
        Files.createDirectories(cachePath)
        thread(name = "FileCachedContentFactory cleaner thread.", isDaemon = true, start = true) {
            while (!this@CachedContentFactory.closed) {
                try {
                    val reference = this.referenceQueue.poll() as? CachedItem
                    if (reference != null) {
                        reference.purge()
                    } else {
                        Thread.sleep(100)
                    }
                } catch (e: InterruptedException) {
                    logger.info { "FileCachedContentFactory cleaner thread interrupted" }
                }
            }

            /* Purge all remaining items. */
            this.refSet.forEach { it.purge() }
        }
    }

    /**
     * Generates the next [Path] for this [CachedContentFactory].
     *
     * @return Next [Path].
     */
    private fun nextPath(): Path = this.cachePath.resolve(this.counter.incrementAndGet().toString())

    override fun newImageContent(bufferedImage: BufferedImage): ImageContent {
        check(!this.closed) { "CachedContentFactory has been closed." }
        val content = CachedImageContent(this.nextPath(), bufferedImage)
        this.refSet.add(CachedItem(content, this.referenceQueue))
        return content
    }

    override fun newAudioContent(channels: Short, sampleRate: Int, audio: ShortBuffer): AudioContent {
        check(!this.closed) { "CachedContentFactory has been closed." }
        val content = CachedAudioContent(this.nextPath(), channels, sampleRate, audio)
        this.refSet.add(CachedItem(content, this.referenceQueue))
        return content
    }

    override fun newTextContent(text: String): TextContent {
        check(!this.closed) { "CachedContentFactory has been closed." }
        val content = CachedTextContent(this.nextPath(), text)
        this.refSet.add(CachedItem(content, this.referenceQueue))
        return content
    }

    override fun newMeshContent(model3D: Model3D): Model3DContent {
        check(!this.closed) { "CachedContentFactory has been closed." }
        TODO()
    }

    /**
     * A [PhantomReference] implementation that allows for the puring of [CachedContent] from the cache.
     */
    inner class CachedItem(referent: CachedContent, queue: ReferenceQueue<in CachedContent>) : PhantomReference<CachedContent>(referent, queue) {
        /** The cache path. */
        val path = referent.path

        /** Purges the content from the cache by deleting the file. */
        fun purge() = try {
            Files.deleteIfExists(this.path)
        } catch (e: IOException) {
            logger.error(e) { "Failed to purge cached item $path." }
        } finally {
            this@CachedContentFactory.refSet.remove(this)
            logger.debug { "Purged cached item $path." }
        }
    }

    /**
     * Closes this [CachedContentFactory] and purges all remaining items. A closed [CachedContentFactory] cannot be used anymore.
     */
    override fun close() {
        this.closed = true
    }
}