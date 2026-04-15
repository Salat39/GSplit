package domain.result

interface ActionResult<out D, out E : ActionError> {
    data class Success<out D, out E : ActionError>(val data: D) : ActionResult<D, E>
    data class Error<out D, out E : ActionError>(val error: E) : ActionResult<D, E>
}
