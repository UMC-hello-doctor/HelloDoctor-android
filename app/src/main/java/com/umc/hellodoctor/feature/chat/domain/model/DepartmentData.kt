package com.umc.hellodoctor.feature.chat.domain.model

object DepartmentData {
    val departments = mapOf(
        "gastro" to Department(
            "dept_gastro",
            listOf("symptom_stomachache", "symptom_indigestion", "symptom_vomit"),
            mapOf(
                "P" to "dept_gastro_p",
                "Q" to "dept_gastro_q",
                "R" to "dept_gastro_r",
                "S" to "dept_gastro_s",
                "T" to "dept_gastro_t",
                "A" to "dept_gastro_a"
            ),
            listOf("redflag_internal_chest", "redflag_internal_breathing", "redflag_internal_gi_bleeding")
        ),
        "derm" to Department(
            "dept_derm",
            listOf("symptom_itchy", "symptom_hives", "symptom_rash"),
            mapOf(
                "P" to "dept_derm_p",
                "Q" to "dept_derm_q",
                "R" to "dept_derm_r",
                "S" to "dept_derm_s",
                "T" to "dept_derm_t",
                "A" to "dept_derm_a"
            ),
            listOf("redflag_derm_anaphylaxis", "redflag_derm_systemic")
        ),
        "ent" to Department(
            "dept_ent",
            listOf("symptom_sore_throat", "symptom_stuffy_nose", "symptom_ear_pain"),
            mapOf("P" to "dept_ent_p", "Q" to "dept_ent_q", "R" to "dept_ent_r", "S" to "dept_ent_s", "T" to "dept_ent_t", "A" to "dept_ent_a"),
            listOf("redflag_ent_airway", "redflag_ent_infection")
        ),
        "resp" to Department(
            "dept_resp",
            listOf("symptom_dyspnea", "symptom_chest_tight", "symptom_cough"),
            mapOf("P" to "dept_resp_p", "Q" to "dept_resp_q", "R" to "dept_resp_r", "S" to "dept_resp_s", "T" to "dept_resp_t", "A" to "dept_resp_a"),
            listOf("redflag_internal_chest", "redflag_internal_breathing")
        ),
        "oph" to Department(
            "dept_oph",
            listOf("symptom_eye_pain", "symptom_blurry_vision", "symptom_red_eye"),
            mapOf("P" to "dept_oph_p", "Q" to "dept_oph_q", "R" to "dept_oph_r", "S" to "dept_oph_s", "T" to "dept_oph_t", "A" to "dept_oph_a"),
            listOf("redflag_oph_vision_loss", "redflag_oph_severe_pain_vomit")
        ),
        "urol" to Department(
            "dept_urol",
            listOf("symptom_dysuria", "symptom_hematuria", "symptom_flank_pain"),
            mapOf("P" to "dept_urol_p", "Q" to "dept_urol_q", "R" to "dept_urol_r", "S" to "dept_urol_s", "T" to "dept_urol_t", "A" to "dept_urol_a"),
            listOf("redflag_urol_flank_fever", "redflag_urol_urinary_retention")
        ),
        "neuro" to Department(
            "dept_neuro",
            listOf("symptom_dizzy", "symptom_numbness", "symptom_severe_headache"),
            mapOf("P" to "dept_neuro_p", "Q" to "dept_neuro_q", "R" to "dept_neuro_r", "S" to "dept_neuro_s", "T" to "dept_neuro_t", "A" to "dept_neuro_a"),
            listOf("redflag_neuro_fast", "redflag_neuro_thunderclap_headache")
        ),
        "gyne" to Department(
            "dept_gyne",
            listOf("symptom_dysmenorrhea", "symptom_lower_abd_pain", "symptom_abnormal_bleeding"),
            mapOf("P" to "dept_gyne_p", "Q" to "dept_gyne_q", "R" to "dept_gyne_r", "S" to "dept_gyne_s", "T" to "dept_gyne_t", "A" to "dept_gyne_a"),
            listOf("redflag_gyne_heavy_bleeding", "redflag_gyne_ectopic")
        ),
        "dent" to Department(
            "dept_dent",
            listOf("symptom_tooth_pain", "symptom_gum_swelling", "symptom_sensitive_cold"),
            mapOf("P" to "dept_dent_p", "Q" to "dept_dent_q", "R" to "dept_dent_r", "S" to "dept_dent_s", "T" to "dept_dent_t", "A" to "dept_dent_a"),
            listOf("redflag_dent_ludwig")
        )
    )
}
