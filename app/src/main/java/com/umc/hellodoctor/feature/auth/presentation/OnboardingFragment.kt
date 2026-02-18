package com.umc.hellodoctor.feature.auth.presentation

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.umc.hellodoctor.R
import com.umc.hellodoctor.app.MainActivity
import com.umc.hellodoctor.databinding.FragmentOnboardingBinding

class OnboardingFragment : Fragment(R.layout.fragment_onboarding) {

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!

    private lateinit var dots: List<ImageView>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentOnboardingBinding.bind(view)

        val pages = listOf(
            OnboardingPage(
                title = "헬로닥터는 당신을 위한\n의료 도우미 앱입니다",
                highlight = "헬로닥터",
                desc = "",
                image1 = R.drawable.onboarding_1,
                showStartButton = false
            ),
            OnboardingPage(
                title = "의료진에게 정확한 증상을 전달하세요",
                highlight = "의료진에게 정확한 증상을 전달하세요",
                desc = "AI를 기반으로 나의 증상을 분석하여\n다양한 언어로 번역할 수 있어요.",
                image1 = R.drawable.onboarding_2_1,
                image2 = R.drawable.onboarding_2_2
            ),
            OnboardingPage(
                title = "주변 병원의 정보를 확인하세요",
                highlight = "주변 병원의 정보를 확인하세요",
                desc = "입력된 증상을 기반으로 주변의 병원을\n찾아줘요.",
                image1 = R.drawable.onboarding_3
            ),
            OnboardingPage(
                title = "어려운 약의 정보 한눈으로 확인해요",
                highlight = "어려운 약의 정보 한눈으로 확인해요",
                desc = "약 이름이나 처방전을 스캔하여 약물의 정보를\n확인하세요.",
                image1 = R.drawable.onboarding_4_1,
                image2 = R.drawable.onboarding_4_2,
                showStartButton = true
            )
        )

        val adapter = OnboardingAdapter(pages) {
            // 시작하기 -> 홈 메뉴로
            (activity as? MainActivity)?.openHomeMenuFromOnboarding()
        }

        binding.Onboarding.adapter = adapter

        setupDots(pages.size)
        selectDot(0)

        binding.Onboarding.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                selectDot(position)
            }
        })
    }

    private fun setupDots(count: Int) {
        val container: LinearLayout = binding.dots
        container.removeAllViews()

        dots = List(count) {
            ImageView(requireContext()).apply {
                setImageResource(R.drawable.dot_unselected)
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                lp.marginEnd = if (it == count - 1) 0 else dp(6)
                layoutParams = lp
                container.addView(this)
            }
        }
    }

    private fun selectDot(index: Int) {
        dots.forEachIndexed { i, iv ->
            iv.setImageResource(if (i == index) R.drawable.dot_selected else R.drawable.dot_unselected)
        }
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
