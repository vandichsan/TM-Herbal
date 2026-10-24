package com.akacaraka.herbal

import android.annotation.SuppressLint
import android.content.Context

object AppContextHolder {
    @SuppressLint("StaticFieldLeak")
    lateinit var context: Context
}
