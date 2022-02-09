package dev.fritz2.dom.html

import dev.fritz2.binding.Handler
import dev.fritz2.lenses.LensException
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.plus
import org.w3c.dom.events.Event

/**
 * Prints [Exception] to error-[console] by ignoring [LensException].
 */
internal fun printErrorIgnoreLensException(throwable: Throwable) {
    when(throwable) {
        is LensException -> {}
        else -> console.error("ERROR: ${throwable.message}", throwable)
    }
}

/**
 * Marks a class that it has a [Job] to start coroutines with.
 */
interface WithJob {

    /**
     * [Job] for launching coroutines in.
     */
    val job: Job

    /**
     * Default error handler printing the error to console.
     *
     * @param exception Exception to handle
     */
    fun errorHandler(exception: Throwable) = ::printErrorIgnoreLensException

    /**
     * Connects a [Flow] to a [Handler].
     *
     * @param handler [Handler] that will be called for each action/event on the [Flow]
     * @receiver [Flow] of action/events to bind to a [Handler]
     */
    infix fun <A> Flow<A>.handledBy(handler: Handler<A>) = handler.collect(this, job)

    /**
     * Connects a [Flow] to a suspendable [execute] function.
     *
     * @param execute function that will be called for each action/event on the [Flow]
     * @receiver [Flow] of action/events to bind to
     */
    infix fun <A> Flow<A>.handledBy(execute: suspend (A) -> Unit) =
        this.onEach { execute(it) }.catch { errorHandler(it) }.launchIn(MainScope() + job)


    /**
     * Connects [Event]s to a [Handler].
     *
     * @receiver [Flow] which contains the [Event]
     * @param handler that will handle the fired [Event]
     */
    infix fun <E : Event> Flow<E>.handledBy(handler: Handler<Unit>) =
        handler.collect(this.map {  }, job)

    /**
     * Connects a [Flow] to a suspendable [execute] function.
     *
     * @receiver [Flow] which contains the [Event]
     * @param execute function that will handle the fired [Event]
     */
    infix fun <E : Event> Flow<E>.handledBy(execute: suspend (E) -> Unit) =
        this.onEach { execute(it) }.catch { errorHandler(it) }.launchIn(MainScope() + job)
}

/**
 * Connects a [Flow] to a [Handler].
 *
 * @param handler [Handler] that will be called for each action/event on the [Flow]
 * @receiver [Flow] of action/events to bind to an [Handler]
 */
infix fun <A> Flow<A>.handledBy(handler: Handler<A>) = handler.collect(this, Job())

/**
 * Connects a [Flow] to a suspendable [execute] function.
 *
 * @param execute function that will be called for each action/event on the [Flow]
 * @receiver [Flow] of action/events to bind to an [Handler]
 */
infix fun <A> Flow<A>.handledBy(execute: suspend (A) -> Unit) =
    this.onEach { execute(it) }.catch { printErrorIgnoreLensException(it) }.launchIn(MainScope() + Job())