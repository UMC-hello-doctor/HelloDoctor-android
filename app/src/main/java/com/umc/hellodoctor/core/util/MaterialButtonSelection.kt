package com.umc.hellodoctor.util

import com.google.android.material.button.MaterialButton

// 한 그룹에서 하나만 선택되게
fun singleSelect(selected: MaterialButton, group: List<MaterialButton>) {
    group.forEach { it.isSelected = (it == selected) }
}
// 토글 선택(여러 개 선택 가능)
fun toggleSelect(button: MaterialButton) {
    button.isSelected = !button.isSelected
}
// state 값 기준으로 버튼들을 선택 표시
fun applySingleSelect(selected: MaterialButton?, group: List<MaterialButton>) {
    group.forEach { it.isSelected = (it == selected) }
}
