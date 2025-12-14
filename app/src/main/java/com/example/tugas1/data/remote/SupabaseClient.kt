package com.example.tugas1.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.user.UserSession
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseClient {


    private const val SUPABASE_URL = "https://wymgmzsgvfwtqxjylsqm.supabase.co"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Ind5bWdtenNndmZ3dHF4anlsc3FtIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjUxMDIwNzQsImV4cCI6MjA4MDY3ODA3NH0.Cbhu632SkWWbpd-jg7XUMGSlyi7_eLDjmvaQlHkZ8dI"

    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Auth)
        install(Postgrest)
        install(Storage)
    }

    fun session(): UserSession? = client.auth.currentSessionOrNull()
}