package com.umc.hellodoctor.feature.chat.domain.model

data class Department(
    // "dept_gastro"
    val nameKey: String,
    // 증상 키워드
    val keywordKeys: List<String>,
    // "P" -> "dept_gastro_p"
    val pqrstKeys: Map<String, String>,
    // "redflag_internal_chest"
    val redflagKeys: List<String>,
)
