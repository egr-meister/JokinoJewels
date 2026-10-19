package com.jokinojewels.game.ui

import com.jokinojewels.game.R

/**
 * Single access point for every image in res/drawable-nodpi.
 * Replace the files (same names) with new art — no code change needed.
 * If an image's proportions change, update the matching *_ASPECT constant (width / height).
 * `tools/import_assets.py` regenerates all of these from the artist's source folder.
 */
object Assets {
    val bgMain: Int = R.drawable.bg_main
    val logo: Int = R.drawable.logo
    val btnMenu: Int = R.drawable.btn_menu
    val btnPlate: Int = R.drawable.btn_plate
    val plateRound: Int = R.drawable.plate_round
    val iconBack: Int = R.drawable.icon_back
    val iconPause: Int = R.drawable.icon_pause
    val frameQuestion: Int = R.drawable.frame_question

    // Six jester poses.
    val mascotA: Int = R.drawable.mascot_a // presenting, ruby in hand
    val mascotB: Int = R.drawable.mascot_b // raising a floating ruby
    val mascotC: Int = R.drawable.mascot_c // welcoming gesture
    val mascotD: Int = R.drawable.mascot_d // dancing with a ruby
    val mascotE: Int = R.drawable.mascot_e // theatrical bow
    val mascotF: Int = R.drawable.mascot_f // standing, ruby held up

    // Decorations.
    val decoCap: Int = R.drawable.deco_cap
    val decoFan: Int = R.drawable.deco_fan
    val decoPendant: Int = R.drawable.deco_pendant
    val decoMedal: Int = R.drawable.deco_medal
    val decoFeather: Int = R.drawable.deco_feather
    val decoTicket: Int = R.drawable.deco_ticket

    /** The 12 Matching Pairs icons (also used as decorative gems). */
    val items: List<Int> = listOf(
        R.drawable.item_01, R.drawable.item_02, R.drawable.item_03, R.drawable.item_04,
        R.drawable.item_05, R.drawable.item_06, R.drawable.item_07, R.drawable.item_08,
        R.drawable.item_09, R.drawable.item_10, R.drawable.item_11, R.drawable.item_12
    )

    /** Text alternatives for the 12 match icons (screen readers, color-independent cue). */
    val itemNames: List<String> = listOf(
        "Royal crown", "Jester mask", "Heart jewel box", "Treasure chest", "Gift box", "Ruby goblet",
        "Golden key", "Hourglass", "Golden bell", "Golden harp", "Jester scepter", "Golden lyre"
    )

    fun item(index: Int): Int = items[Math.floorMod(index, items.size)]

    fun itemName(index: Int): String = itemNames[Math.floorMod(index, itemNames.size)]

    /** Decoration shown for each quiz difficulty (1 = easy … 3 = hard). */
    fun difficultyIcon(difficulty: Int): Int = when (difficulty) {
        1 -> decoPendant
        2 -> decoFan
        else -> decoCap
    }

    fun difficultyIconAspect(difficulty: Int): Float = when (difficulty) {
        1 -> DECO_PENDANT_ASPECT
        2 -> DECO_FAN_ASPECT
        else -> DECO_CAP_ASPECT
    }

    const val LOGO_ASPECT = 837f / 559f
    const val BTN_MENU_ASPECT = 1000f / 347f
    /** Vertical centre of the blue label area on btn_menu (the crest sits above it). */
    const val BTN_MENU_LABEL_Y = 0.70f
    /** Height of the blue label area on btn_menu as a fraction of the whole plate. */
    const val BTN_MENU_LABEL_H = 0.44f
    const val BTN_PLATE_ASPECT = 1000f / 204f
    const val FRAME_QUESTION_ASPECT = 1000f / 665f

    const val MASCOT_A_ASPECT = 553f / 900f
    const val MASCOT_B_ASPECT = 422f / 900f
    const val MASCOT_C_ASPECT = 553f / 900f
    const val MASCOT_D_ASPECT = 571f / 900f
    const val MASCOT_E_ASPECT = 571f / 900f
    const val MASCOT_F_ASPECT = 422f / 900f

    const val DECO_CAP_ASPECT = 480f / 386f
    const val DECO_FAN_ASPECT = 480f / 310f
    const val DECO_PENDANT_ASPECT = 442f / 480f
    const val DECO_MEDAL_ASPECT = 283f / 480f
    const val DECO_FEATHER_ASPECT = 474f / 480f
    const val DECO_TICKET_ASPECT = 480f / 293f
}
