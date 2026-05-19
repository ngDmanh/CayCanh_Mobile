package com.example.caycanh_mobile

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Class Application của app — Hilt yêu cầu để khởi tạo DI container.
 */
@HiltAndroidApp
class CayCanhApplication : Application()