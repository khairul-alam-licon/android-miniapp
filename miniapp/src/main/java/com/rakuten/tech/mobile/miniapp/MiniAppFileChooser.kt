package com.rakuten.tech.mobile.miniapp

import android.net.Uri
import android.webkit.ValueCallback

interface MiniAppFileChooser {

    var getFile: ValueCallback<Array<Uri>>?
}
