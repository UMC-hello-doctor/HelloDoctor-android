package com.umc.hellodoctor.feature.chat.domain.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * DepartmentRepository - XML에서 진료과 관련 질문과 정보를 관리
 */
class DepartmentRepository
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        /**
         * 진료과 키에 해당하는 모든 질문(P, Q, R, S, T, A)을 맵으로 반환
         * @param deptKey 진료과 키 (예: "gastro", "derm", "ent", "resp" 등)
         * @return 질문 타입(P, Q, R, S, T, A)과 질문 텍스트의 맵
         */
        @Suppress("TooGenericExceptionCaught", "SwallowedException")
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
                    "A" to getString(resources, packageName, "dept_${deptKey}_a"),
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
            return DEPARTMENT_KEY_MAP.entries
                .firstOrNull { (_, keywords) -> keywords.any { lowerName.contains(it) } }
                ?.key
                ?: run {
                    android.util.Log.e(TAG, "알 수 없는 진료과: $departmentName")
                    ""
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
            } catch (e: android.content.res.Resources.NotFoundException) {
                Log.e("DepartmentRepository", "Department name not found for key: $deptKey", e)
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
                    "내과" to
                        listOf(
                            getString(resources, packageName, "redflag_internal_chest"),
                            getString(resources, packageName, "redflag_internal_breathing"),
                            getString(resources, packageName, "redflag_internal_gi_bleeding"),
                        ),
                    "피부과" to
                        listOf(
                            getString(resources, packageName, "redflag_derm_anaphylaxis"),
                            getString(resources, packageName, "redflag_derm_systemic"),
                        ),
                    "이비인후과" to
                        listOf(
                            getString(resources, packageName, "redflag_ent_airway"),
                            getString(resources, packageName, "redflag_ent_infection"),
                        ),
                    "안과" to
                        listOf(
                            getString(resources, packageName, "redflag_oph_vision_loss"),
                            getString(resources, packageName, "redflag_oph_severe_pain_vomit"),
                        ),
                    "비뇨의학과" to
                        listOf(
                            getString(resources, packageName, "redflag_urol_flank_fever"),
                            getString(resources, packageName, "redflag_urol_urinary_retention"),
                        ),
                    "신경과" to
                        listOf(
                            getString(resources, packageName, "redflag_neuro_fast"),
                            getString(resources, packageName, "redflag_neuro_thunderclap_headache"),
                        ),
                    "산부인과" to
                        listOf(
                            getString(resources, packageName, "redflag_gyne_heavy_bleeding"),
                            getString(resources, packageName, "redflag_gyne_ectopic"),
                        ),
                    "치과" to
                        listOf(
                            getString(resources, packageName, "redflag_dent_ludwig"),
                        ),
                ).mapValues { (_, questions) -> questions.filter { it.isNotEmpty() } }
            } catch (e: android.content.res.Resources.NotFoundException) {
                android.util.Log.w("DepartmentRepository", "getAllRedFlagQuestions 로드 실패", e)
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

        private fun getString(
            resources: android.content.res.Resources,
            packageName: String,
            resourceName: String,
        ): String {
            return try {
                val resId = resources.getIdentifier(resourceName, "string", packageName)
                if (resId != 0) resources.getString(resId) else ""
            } catch (e: android.content.res.Resources.NotFoundException) {
                android.util.Log.w("DepartmentRepository", "getString 실패: $resourceName", e)
                ""
            }
        }

        companion object {
            private const val TAG = "DepartmentRepository"

            private val DEPARTMENT_KEY_MAP =
                mapOf(
                    "gastro" to listOf("소화기", "위장", "gastro"),
                    "derm" to listOf("피부", "derm"),
                    "ent" to listOf("이비인후", "ent", "ear", "nose", "throat"),
                    "resp" to listOf("호흡기", "순환기", "respiratory", "cardio", "pulmonary"),
                    "oph" to listOf("안과", "oph", "eye"),
                    "urol" to listOf("비뇨", "urol"),
                    "neuro" to listOf("신경과", "neuro"),
                    "gyne" to listOf("산부인과", "부인", "gyne", "obstetric"),
                    "dent" to listOf("치과", "dent"),
                    "ortho" to listOf("정형", "orthop"),
                    "internal" to listOf("내과", "internal"),
                    "pedi" to listOf("소아", "pediatric"),
                )
        }
    }
