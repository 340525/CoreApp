package com.t.core
import android.inputmethodservice.InputMethodService
import android.view.View
import android.widget.TextView
class ImeService : InputMethodService() {
    companion object { var inst: ImeService? = null; fun commit(s: String) { inst?.currentInputConnection?.commitText(s, 1) } }
    override fun onCreate() { super.onCreate(); inst = this }
    override fun onDestroy() { inst = null; super.onDestroy() }
    override fun onCreateInputView(): View = TextView(this)
}
