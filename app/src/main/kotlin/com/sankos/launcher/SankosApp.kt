package com.sankos.launcher

import android.app.Application

/**
 * SankOS application entry point.
 *
 * Deliberately minimal: no background services, no network, no analytics.
 * All state lives either in per-screen view models or in DataStore.
 */
class SankosApp : Application()
