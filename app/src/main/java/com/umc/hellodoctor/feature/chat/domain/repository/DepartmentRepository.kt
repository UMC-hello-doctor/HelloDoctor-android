package com.umc.hellodoctor.feature.chat.domain.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * DepartmentRepository - XML에서 진료과 관련 질문과 정보를 관리
 */
class DepartmentRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * 진료과 키에 해당하는 모든 질문(P, Q, R, S, T, A)을 맵으로 반환
     * @param deptKey 진료과 키 (예: "gastro", "derm", "ent", "resp" 등)
     * @return 질문 타입(P, Q, R, S, T, A)과 질문 텍스트의 맵
     */
    fun getDepartmentQuestions(deptKey: String): Map<String, String> {
        val resources = context.resources
        val packageName = context.packageName

        return try {
            mapOf(
                "P" to getString(resources, packageName, "dept_${deptKey}_p"),
                "Q" to getString(resources, packageName, "dept_${deptKey}_q"),
                "R" to getString(resources, packageName, "dept_${deptKey}_r"),
                "S" to getString(resources, packageName, "dept_${deptKey}_s"),
                "T" to getString(resources, packageName, "dept_${deptKey}_t"),
                "A" to getString(resources, packageName, "dept_${deptKey}_a")
            ).filterValues { it.isNotEmpty() }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    /**
     * 진료과명에 해당하는 키를 반환
     * @param departmentName 진료과명 (예: "내과 - 소화기", "Gastroenterology")
     * @return 진료과 키 (예: "gastro")
     */
    fun getDepartmentKey(departmentName: String): String {
        val lowerName = departmentName.lowercase()
        return when {
            // 소화기내과 / Gastroenterology
            lowerName.contains("소화기") || lowerName.contains("위장") ||
            lowerName.contains("gastro") -> "gastro"

            // 피부과 / Dermatology
            lowerName.contains("피부") || lowerName.contains("derm") -> "derm"

            // 이비인후과 / ENT (Ear, Nose, Throat)
            lowerName.contains("이비인후") || lowerName.contains("ent") ||
            lowerName.contains("ear") || lowerName.contains("nose") || lowerName.contains("throat") -> "ent"

            // 호흡기/순환기내과 / Respiratory/Cardiology
            lowerName.contains("호흡기") || lowerName.contains("순환기") ||
            lowerName.contains("respiratory") || lowerName.contains("cardio") ||
            lowerName.contains("pulmonary") -> "resp"

            // 안과 / Ophthalmology
            lowerName.contains("안과") || lowerName.contains("oph") ||
            lowerName.contains("eye") -> "oph"

            // 비뇨의학과 / Urology
            lowerName.contains("비뇨") || lowerName.contains("urol") -> "urol"

            // 신경과 / Neurology
            lowerName.contains("신경과") || lowerName.contains("neuro") -> "neuro"

            // 산부인과 / Gynecology/Obstetrics
            lowerName.contains("산부인과") || lowerName.contains("부인") ||
            lowerName.contains("gyne") || lowerName.contains("obstetric") -> "gyne"

            // 치과 / Dentistry
            lowerName.contains("치과") || lowerName.contains("dent") -> "dent"

            // 정형외과 / Orthopedics
            lowerName.contains("정형") || lowerName.contains("orthop") -> "ortho"

            // 내과 / Internal Medicine
            lowerName.contains("내과") || lowerName.contains("internal") -> "internal"

            // 소아청소년과 / Pediatrics
            lowerName.contains("소아") || lowerName.contains("pediatric") -> "pedi"

            else -> {
                android.util.Log.e("DepartmentRepository", "알 수 없는 진료과: $departmentName")
                ""
            }
        }
    }

    /**
     * 진료과명을 리소스에서 조회
     * @param deptKey 진료과 키
     * @return 진료과명
     */
    fun getDepartmentName(deptKey: String): String {
        val resources = context.resources
        val packageName = context.packageName
        return try {
            getString(resources, packageName, "dept_$deptKey")
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * 모든 진료과의 Red Flag 질문들을 반환
     * @return 진료과별 Red Flag 질문 맵
     */
    fun getAllRedFlagQuestions(): Map<String, List<String>> {
        val resources = context.resources
        val packageName = context.packageName

        return try {
            mapOf(
                "내과" to listOf(
                    getString(resources, packageName, "redflag_internal_chest"),
                    getString(resources, packageName, "redflag_internal_breathing"),
                    getString(resources, packageName, "redflag_internal_gi_bleeding")
                ),
                "피부과" to listOf(
                    getString(resources, packageName, "redflag_derm_anaphylaxis"),
                    getString(resources, packageName, "redflag_derm_systemic")
                ),
                "이비인후과" to listOf(
                    getString(resources, packageName, "redflag_ent_airway"),
                    getString(resources, packageName, "redflag_ent_infection")
                ),
                "안과" to listOf(
                    getString(resources, packageName, "redflag_oph_vision_loss"),
                    getString(resources, packageName, "redflag_oph_severe_pain_vomit")
                ),
                "비뇨의학과" to listOf(
                    getString(resources, packageName, "redflag_urol_flank_fever"),
                    getString(resources, packageName, "redflag_urol_urinary_retention")
                ),
                "신경과" to listOf(
                    getString(resources, packageName, "redflag_neuro_fast"),
                    getString(resources, packageName, "redflag_neuro_thunderclap_headache")
                ),
                "산부인과" to listOf(
                    getString(resources, packageName, "redflag_gyne_heavy_bleeding"),
                    getString(resources, packageName, "redflag_gyne_ectopic")
                ),
                "치과" to listOf(
                    getString(resources, packageName, "redflag_dent_ludwig")
                )
            ).mapValues { (_, questions) -> questions.filter { it.isNotEmpty() } }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    /**
     * 특정 진료과의 Red Flag 질문들을 반환
     * @param deptName 진료과명
     * @return Red Flag 질문 리스트
     */
    fun getRedFlagQuestions(deptName: String): List<String> {
        return getAllRedFlagQuestions()[deptName] ?: emptyList()
    }

    private fun getString(resources: android.content.res.Resources, packageName: String, resourceName: String): String {
        return try {
            val resId = resources.getIdentifier(resourceName, "string", packageName)
            if (resId != 0) resources.getString(resId) else ""
        } catch (e: Exception) {
            ""
        }
    }
}

