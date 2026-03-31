package com.umc.hellodoctor.feature.chat.presentation

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.umc.hellodoctor.feature.navermap.presentation.NaverMapFragment

class ChatResultPagerAdapter(
    fragmentActivity: FragmentActivity,
) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FieldModeFragment()
            1 -> {
                NaverMapFragment()
            }
            else -> error("Invalid position $position")
        }
    }
}
