package domain

interface UseCase<in Params, out Result> {
    suspend fun execute(params: Params): Result
}
