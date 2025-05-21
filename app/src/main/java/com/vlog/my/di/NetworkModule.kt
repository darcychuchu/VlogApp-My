package com.vlog.my.di

import android.content.Context
import android.os.Build
import androidx.work.WorkManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.vlog.my.data.LocalDataHelper
import com.vlog.my.data.bazaar.BazaarScriptsDao
import com.vlog.my.data.bazaar.BazaarScriptsDatabase
import com.vlog.my.data.bazaar.BazaarScriptsService
import com.vlog.my.data.scripts.SubScriptsDataHelper
import com.vlog.my.data.scripts.articles.ArticlesScriptsService
import com.vlog.my.data.scripts.articles.ArticlesScriptsDataHelper
import com.vlog.my.data.stories.StoriesService
import com.vlog.my.data.users.UserService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.LoggingEventListener
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Singleton
import kotlin.apply
import kotlin.jvm.java

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    val APP_INFO = "${Build.BRAND}-${Build.PRODUCT}-VlogApp-${Constants.APP_VERSION}-0"

    val provideAuthInterceptor =  Interceptor { chain: Interceptor.Chain ->
        val initialRequest = chain.request()
        val newUrl = initialRequest.url.newBuilder()
            .addQueryParameter("app_info", APP_INFO)
            .build()
        val newRequest = initialRequest.newBuilder()
            .url(newUrl)
            .build()
        chain.proceed(newRequest)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .apply { eventListenerFactory(LoggingEventListener.Factory()) }
            .addInterceptor(provideAuthInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit {
        val baseUrl = Constants.API_BASE_URL
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    fun provideUserService(retrofit: Retrofit): UserService {
        return retrofit.create(UserService::class.java)
    }

    @Provides
    @Singleton
    fun provideStoriesService(retrofit: Retrofit): StoriesService {
        return retrofit.create(StoriesService::class.java)
    }


    @Provides
    @Singleton
    fun provideArticlesScriptsService(okHttpClient: OkHttpClient, moshi: Moshi): ArticlesScriptsService {
        val baseUrl = Constants.API_BASE_URL
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ArticlesScriptsService::class.java)
    }

    @Provides
    @Singleton
    fun provideLocalDataHelper(@ApplicationContext context: Context): LocalDataHelper {
        return LocalDataHelper(context)
    }

    @Provides
    @Singleton
    fun provideSubScriptsDataHelper(@ApplicationContext context: Context): SubScriptsDataHelper {
        return SubScriptsDataHelper(context)
    }

    @Provides
    @Singleton
    fun provideArticlesScriptsDataHelper(@ApplicationContext context: Context): ArticlesScriptsDataHelper {
        return ArticlesScriptsDataHelper(context)
    }

    @Singleton
    @Provides
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager = WorkManager.getInstance(context)



    @Provides
    @Singleton
    fun provideBazaarScriptService(retrofit: Retrofit): BazaarScriptsService {
        return retrofit.create(BazaarScriptsService::class.java)
    }



    /**
     * 小程序服务器数据库模块
     */
    @Provides
    @Singleton
    fun provideBazaarScriptsDatabase(@ApplicationContext context: Context): BazaarScriptsDatabase {
        return BazaarScriptsDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideBazaarScriptsDao(bazaarScriptsDatabase: BazaarScriptsDatabase): BazaarScriptsDao {
        return bazaarScriptsDatabase.bazaarScriptsDao()
    }

}
