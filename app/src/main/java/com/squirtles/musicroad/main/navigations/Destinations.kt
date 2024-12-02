package com.squirtles.musicroad.main.navigations

object MainDestinations {
    const val MAIN_ROUTE = "main"
    const val FAVORITE_PICKS_ROUTE = "favorite_picks"
    const val MY_PICKS_ROUTE = "my_picks"
}

object CreatePickDestinations {
    const val CREATE_ROUTE = "create"
    const val SEARCH_MUSIC_ROUTE = "search_music"
    const val CREATE_PICK_ROUTE = "create_pick"
}

object ProfileDestination {
    private const val PROFILE_ROUTE = "profile"
    const val SETTING_PROFILE_ROUTE = "setting/profile"
    const val SETTING_NOTIFICATION_ROUTE = "setting/notification"

    fun profile(userId: String) = "$PROFILE_ROUTE/$userId"
}

object PickInfoDestinations {
    private const val PICK_DETAIL_ROUTE = "pick_detail"

    fun pickDetail(pickId: String) = "$PICK_DETAIL_ROUTE/$pickId"
}
