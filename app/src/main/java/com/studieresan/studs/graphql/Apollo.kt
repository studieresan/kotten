package com.studieresan.studs.graphql

import android.content.Context
import android.os.Looper
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.CustomTypeAdapter
import com.apollographql.apollo.api.CustomTypeValue
import com.studieresan.studs.data.StudsPreferences
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import type.CustomType
import java.text.ParseException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private var instance: ApolloClient? = null
val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

fun apolloClient(context: Context): ApolloClient {


    check(Looper.myLooper() == Looper.getMainLooper()) {
        "Only the main thread can get the apolloClient instance"
    }

    if (instance != null) {
        return instance!!
    }

    instance = ApolloClient.builder()
            .serverUrl("https://studs-overlord.herokuapp.com/graphql")
            .okHttpClient(OkHttpClient.Builder()
                    .addInterceptor(AuthorizationInterceptor(context))
                    .build()
            )
            .addCustomTypeAdapter(CustomType.DATETIME, DateTimeCustomTypeAdapter)
            .build()

    return instance!!
}

val DateTimeCustomTypeAdapter = object : CustomTypeAdapter<LocalDateTime> {
    override fun decode(value: CustomTypeValue<*>): LocalDateTime {

        return try {
            LocalDateTime.parse(value.value.toString(), DATE_FORMATTER)
        } catch (e: ParseException) {
            throw RuntimeException(e)
        }
    }

    override fun encode(value: LocalDateTime): CustomTypeValue<*> {
        return CustomTypeValue.GraphQLString(value.toString())
    }
}

private class AuthorizationInterceptor(val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
                .addHeader("Authorization", StudsPreferences.getJwtToken(context))
                .build()

        return chain.proceed(request)
    }
}
