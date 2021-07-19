package com.studieresan.studs.data.models

data class LoginResponse(
        val id: String,
        val email: String,
        val token: String,
        val name: String,
        val picture: String,
        val position: String,
        val role: String,
        val permissions: List<String>
)
