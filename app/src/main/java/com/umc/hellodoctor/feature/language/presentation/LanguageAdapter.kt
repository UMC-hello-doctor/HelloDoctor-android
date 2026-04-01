package com.umc.hellodoctor.feature.language.presentation

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.umc.hellodoctor.R

class LanguageAdapter(
    context: Context,
    private val items: List<LanguageItem>,
) : ArrayAdapter<LanguageItem>(context, 0, items) {
    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup,
    ): View {
        val view =
            convertView ?: LayoutInflater.from(context)
                .inflate(R.layout.item_language_row, parent, false)

        val item = items[position]
        view.findViewById<ImageView>(R.id.imgFlag).setImageResource(item.iconRes)

        view.findViewById<TextView>(R.id.lang).text = item.label

        return view
    }

    // 사실상 없어도 되지만 남겨도 OK
    override fun getDropDownView(
        position: Int,
        convertView: View?,
        parent: ViewGroup,
    ): View {
        return getView(position, convertView, parent)
    }
}
