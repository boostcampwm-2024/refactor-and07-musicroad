package com.squirtles.musicroad.main.navigations

object MainDestinations {
    const val MAIN_ROUTE = "main"
    const val FAVORITE_ROUTE = "favorite"
    const val SETTING_ROUTE = "setting"
    const val CREATE_ROUTE = "create"
}

object CreatePickDestinations {
    const val SEARCH_ROUTE = "create"
    const val SEARCH_MUSIC_ROUTE = "search_music"
    const val CREATE_PICK_ROUTE = "create_pick"
}

object PickInfoDestinations {
    private const val PICK_DETAIL_ROUTE = "pick_detail"

    fun pickDetail(pickId: String) = "$PICK_DETAIL_ROUTE/$pickId"
}
