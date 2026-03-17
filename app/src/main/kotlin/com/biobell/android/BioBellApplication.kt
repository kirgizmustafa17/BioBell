package com.biobell.android

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * BioBell Application class.
 * Annotated with @HiltAndroidApp to trigger Hilt's code generation
 * and initialize the DI graph.
 */
@HiltAndroidApp
class BioBellApplication : Application()
