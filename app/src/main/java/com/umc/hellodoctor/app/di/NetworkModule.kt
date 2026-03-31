package com.umc.hellodoctor.app.di

import android.content.Context
import com.umc.hellodoctor.BuildConfig
import com.umc.hellodoctor.core.network.AuthInterceptor
import com.umc.hellodoctor.core.network.status.NetworkMonitor
import com.umc.hellodoctor.feature.auth.data.repository.AuthApi
import com.umc.hellodoctor.feature.drug.data.api.MedicineApi
import com.umc.hellodoctor.feature.navermap.data.HospitalApi
import com.umc.hellodoctor.feature.userinfo.data.api.UserInfoApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.SERVER_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideHospitalApi(retrofit: Retrofit): HospitalApi = retrofit.create(HospitalApi::class.java)

    @Provides
    @Singleton
    fun provideMedicineApi(retrofit: Retrofit): MedicineApi = retrofit.create(MedicineApi::class.java)

    @Provides
    @Singleton
    fun provideUserInfoApi(retrofit: Retrofit): UserInfoApi = retrofit.create(UserInfoApi::class.java)

    @Provides
    @Singleton
    fun provideNetworkMonitor(
        @ApplicationContext context: Context,
    ): NetworkMonitor {
        return NetworkMonitor(context)
    }
}
