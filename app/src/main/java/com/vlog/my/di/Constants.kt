package com.vlog.my.di

object Constants {

    //const val APP_WEB = "https://66log.com"

    const val APP_WEB = "https://api.66log.com"
    const val API_BASE_URL = "${APP_WEB}/api/json/v1/"

    const val IMAGE_SIZE_SMALL = "s/"
    const val IMAGE_SIZE_MEDIUM = "m/"
    const val IMAGE_SIZE_BIG = "l/"

    const val IMAGE_BASE_URL = "${APP_WEB}/file/attachments/image/${IMAGE_SIZE_SMALL}"

    const val APP_VERSION = "1.4.0"
}