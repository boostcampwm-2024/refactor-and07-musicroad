package com.squirtles.data.exception

sealed class FirebaseException(override val message: String) : Exception() {
    data class UserNotFoundException(override val message: String = "Failed to fetch a user") : FirebaseException(message)

    data class NoSuchPickException(override val message: String = "No such pick") :
        FirebaseException(message)

    data class NoSuchPickInRadiusException(override val message: String = "No such pick in area") :
        FirebaseException(message)
}
