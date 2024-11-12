package com.squirtles.data.exception

sealed class AppleMusicException(override val message: String) : Exception() {
    data class InvalidParameterException(override val message: String) :
        AppleMusicException(message)

    data class NotFoundException(override val message: String = "No such resource") :
        AppleMusicException(message)
}
