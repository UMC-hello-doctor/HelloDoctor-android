package com.umc.hellodoctor.util

import android.app.DatePickerDialog
import android.content.Context
import java.util.Calendar


// 생년월일 선택용 DatePicker
// 결과 포맷: yyyy-MM-dd
fun showBirthDatePicker(
    context: Context,
    initial: String?,
    onSelected: (String) -> Unit
) {
    val cal = Calendar.getInstance()

    // initial이 "yyyy-MM-dd" 형태면 그 값으로 초기화
    if (!initial.isNullOrBlank() && initial.matches(Regex("""\d{4}-\d{2}-\d{2}"""))) {
        val (y, m, d) = initial.split("-").map { it.toInt() }
        cal.set(y, m - 1, d)
    }

    val year = cal.get(Calendar.YEAR)
    val month = cal.get(Calendar.MONTH)
    val day = cal.get(Calendar.DAY_OF_MONTH)

    DatePickerDialog(context, { _, y, m, d ->
        val result = "%04d-%02d-%02d".format(y, m + 1, d)
        onSelected(result)
    }, year, month, day).show()
}
