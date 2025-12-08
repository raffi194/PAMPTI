package com.example.tugas1.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.user.UserSession
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseClient {


    private const val SUPABASE_URL = "hhttps://rmizbnlbguddedpryumj.supabase.co"
    private const val SUPABASE_KEY = "sb_publishable_KKluEDKAq_-qL6m_kTuFqg_nrVMtbh0"

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

