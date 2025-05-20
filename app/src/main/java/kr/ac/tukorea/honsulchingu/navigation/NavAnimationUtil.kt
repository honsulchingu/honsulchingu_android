package kr.ac.tukorea.honsulchingu.navigation

import androidx.navigation.NavOptions
import kr.ac.tukorea.honsulchingu.R

object NavAnimationUtil {

    fun getSlideFromRightOptions(): NavOptions {
        return NavOptions.Builder()
            .setEnterAnim(R.anim.slide_in_right)
            .setExitAnim(R.anim.slide_out_left)
            .setPopEnterAnim(R.anim.slide_in_left)
            .setPopExitAnim(R.anim.slide_out_right)
            .build()
    }

    fun getSlideFromLeftOptions(): NavOptions {
        return NavOptions.Builder()
            .setEnterAnim(R.anim.slide_in_left)
            .setExitAnim(R.anim.slide_out_right)
            .setPopEnterAnim(R.anim.slide_in_right)
            .setPopExitAnim(R.anim.slide_out_left)
            .build()
    }

    fun getSlideFromBottomOptions(): NavOptions {
        return NavOptions.Builder()
            .setEnterAnim(R.anim.slide_in_bottom)
            .setExitAnim(R.anim.fade_out)
            .setPopEnterAnim(R.anim.fade_in)
            .setPopExitAnim(R.anim.slide_out_bottom)
            .build()
    }

    fun getFadeOptions(popUpToId: Int? = null, inclusive: Boolean = false): NavOptions {
        val builder = NavOptions.Builder()
            .setEnterAnim(R.anim.fade_in)
            .setExitAnim(R.anim.fade_out)
            .setPopEnterAnim(R.anim.fade_in)
            .setPopExitAnim(R.anim.fade_out)
        if (popUpToId != null) {
            builder.setPopUpTo(popUpToId, inclusive)
        }
        return builder.build()
    }



    fun getVoiceChatToChatAnim(): NavOptions {
        return NavOptions.Builder()
            .setEnterAnim(R.anim.slide_in_right) // 오른쪽 -> 왼쪽 슬라이드
            .setExitAnim(R.anim.slide_out_left) // 우측으로 나가기
            .setPopEnterAnim(R.anim.slide_in_left) // 왼쪽에서 오른쪽으로 슬라이드 인
            .setPopExitAnim(R.anim.slide_out_right) // 좌측으로 나가기
            .build()
    }


    fun getBookmarkToChatOptions(): NavOptions {
        return NavOptions.Builder()
            .setEnterAnim(R.anim.zoom_in)
            .setExitAnim(R.anim.zoom_out)
            .setPopEnterAnim(R.anim.zoom_in)
            .setPopExitAnim(R.anim.zoom_out)
            .build()
    }
    fun getSlideUpFadeInOptions(): NavOptions {
        return NavOptions.Builder()
            .setEnterAnim(R.anim.slide_in_bottom_fade)
            .setExitAnim(R.anim.fade_out)
            .setPopEnterAnim(R.anim.fade_in)
            .setPopExitAnim(R.anim.slide_out_bottom_fade)
            .build()
    }

    fun getZoomInOutOptions(): NavOptions {
        return NavOptions.Builder()
            .setEnterAnim(R.anim.zoom_in)
            .setExitAnim(R.anim.zoom_out)
            .setPopEnterAnim(R.anim.zoom_in)
            .setPopExitAnim(R.anim.zoom_out)
            .build()
    }
}