package com.jokinojewels.game.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Visual concept: "Sapphire Jester Theatre" — a royal stage behind deep-blue velvet
 * curtains, where a harlequin jester juggles gold-set rubies and sapphires.
 * The palette is sampled from the delivered art (background, plates, gems).
 *
 * HEX + usage
 *  Midnight        #020B2B  window/splash background, deepest panel stop
 *  Navy            #0A1D5C  surfaces, HUD pills, card-back bottom stop
 *  SapphireDeep    #1636A3  panel top stop, card-back top stop
 *  Sapphire        #2A57D8  card face top stop, secondary accent
 *  SapphireBright  #5B8CFF  timer bar, focus accents, timer pill text
 *  JesterGold      #F6C344  primary accent: borders, titles, progress, filled pips
 *  GoldDeep        #B8862B  secondary borders, dividers
 *  Ruby            #D9263D  urgency accent (always paired with the text "Hurry!")
 *  RubyDeep        #A3162A  WRONG answer fill (white text ≈ 7.6:1)
 *  Emerald         #2FBF71  success accent
 *  EmeraldDeep     #1B7A45  CORRECT answer fill (white text ≈ 5.4:1)
 *  Cream           #FFF6DE  body text on dark surfaces (≈ 15:1 on Navy)
 */
object JewelPalette {
    val Midnight = Color(0xFF020B2B)
    val Navy = Color(0xFF0A1D5C)
    val SapphireDeep = Color(0xFF1636A3)
    val Sapphire = Color(0xFF2A57D8)
    val SapphireBright = Color(0xFF5B8CFF)
    val JesterGold = Color(0xFFF6C344)
    val GoldDeep = Color(0xFFB8862B)
    val Ruby = Color(0xFFD9263D)
    val RubyDeep = Color(0xFFA3162A)
    val Emerald = Color(0xFF2FBF71)
    val EmeraldDeep = Color(0xFF1B7A45)
    val Cream = Color(0xFFFFF6DE)
    val Scrim = Color(0xD9010618)
    val CardFace = Color(0xFF2A57D8)
}
