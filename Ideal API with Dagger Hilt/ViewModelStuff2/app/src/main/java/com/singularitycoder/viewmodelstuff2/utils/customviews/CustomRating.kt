package com.singularitycoder.viewmodelstuff2.utils.customviews

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.singularitycoder.viewmodelstuff2.R
import com.singularitycoder.viewmodelstuff2.databinding.LayoutMyCustomRatingBinding

// Extend the parent container of the layout
class CustomRating : ConstraintLayout {

    private lateinit var binding: LayoutMyCustomRatingBinding

    constructor(context: Context) : super(context)

    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet)


    init {
        inflate(context, R.layout.layout_my_custom_rating, this)
    }

}
