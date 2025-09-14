package kr.ac.tukorea.honsulchingu.ui.onboarding


import java.io.Serializable

data class TutorialData(
    val title: String,
    val imageResId: Int? = null
) : Serializable