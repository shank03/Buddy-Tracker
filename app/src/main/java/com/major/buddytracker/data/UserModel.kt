package com.major.buddytracker.data

data class UserModel(
    val aadharNumber: Long,
    val address: String,
    val category: String,
    val designation: String,
    val dob: String,
    val email: String,
    val gender: String,
    val id: String,
    val idToken: String,
    val isApproved: Boolean,
    val mob: String,
    val password: String,
    val userName: String,
) {
    constructor() : this(
        0, "", "", "", "", "", "", "",
        "", false, "", "", "",
    )
}
