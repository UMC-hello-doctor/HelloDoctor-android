package com.umc.hellodoctor.feature.chat.domain.model

data class Department(
    val nameKey: String,              // "dept_gastro"
    val keywordKeys: List<String>,    // 증상 키워드
    val pqrstKeys: Map<String, String>, // "P" -> "dept_gastro_p"
    val redflagKeys: List<String>     // "redflag_internal_chest"
)