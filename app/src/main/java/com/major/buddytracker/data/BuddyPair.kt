package com.major.buddytracker.data

data class BuddyPair(
    val buddyPairId: String,

    val buddy1Id: String,
    val buddy1CheckInTime: String,

    val buddy2Id: String,
    val buddy2CheckInTime: String,

    val endDateTime: String,
    val startDateTime: String,

    val locationAddress: String,
    val locationCode: String,
    val locationMapAddress: String,
) {
    constructor() : this("", "", "", "", "", "", "", "", "", "")
}
