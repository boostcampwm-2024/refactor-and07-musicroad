package com.squirtles.data.exception

sealed class FirebaseRepositoryException(override val message: String) : Exception() {
    data class NoSuchPickException(override val message: String = "No such pick") :
        FirebaseRepositoryException(message)

    data class NoSuchPickInRadiusException(override val message: String = "No such pick in area") :
        FirebaseRepositoryException(message)
}
