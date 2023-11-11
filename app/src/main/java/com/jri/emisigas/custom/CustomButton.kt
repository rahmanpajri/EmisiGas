package com.jri.emisigas.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.jri.emisigas.R

class CustomButton: AppCompatButton {
    private var backgroundColor: Drawable? = null

    constructor(context: Context) : super(context) {
        init()
    }
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        updateButton()
    }

    private fun init() {
        backgroundColor = ContextCompat.getDrawable(context, R.drawable.bg_home_button)
        isAllCaps = false
        updateButton()
    }

    private fun updateButton() {
        background = backgroundColor
        textSize = 14f
        setTextColor(ContextCompat.getColor(context, android.R.color.black))
    }
}