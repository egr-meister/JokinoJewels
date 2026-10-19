package com.jokinojewels.game.data

data class QuizQuestion(
    val text: String,
    val options: List<String>,
    val correctIndex: Int
)

/**
 * One quiz difficulty level. [difficulty] is 1 (easy), 2 (medium) or 3 (hard).
 */
data class QuizCategory(
    val id: String,
    val title: String,
    val subtitle: String,
    val difficulty: Int,
    val questions: List<QuizQuestion>
)

/**
 * ============================================================================
 *  QUIZ CONTENT — "Round Wonders": circles, spheres and balls.
 *  3 difficulty levels x 10 questions, 4 options each, exactly one correct.
 *  Texts are freely replaceable; keep each `id` stable — best scores are stored under it.
 * ============================================================================
 */
object QuizContent {

    const val QUIZ_TITLE = "Round Wonders"

    val categories: List<QuizCategory> = listOf(
        QuizCategory(
            id = "round_easy",
            title = "Easy",
            subtitle = "Balls, rings and round things",
            difficulty = 1,
            questions = listOf(
                QuizQuestion("The distance from the centre of a circle to its edge is called the…", listOf("Radius", "Diameter", "Chord", "Arc"), 0),
                QuizQuestion("The diameter of a circle is how many times its radius?", listOf("3 times", "2 times", "4 times", "Half"), 1),
                QuizQuestion("Which of these balls is the biggest?", listOf("Tennis ball", "Golf ball", "Basketball", "Ping-pong ball"), 2),
                QuizQuestion("Which planet is famous for its bright, wide rings?", listOf("Mars", "Venus", "Mercury", "Saturn"), 3),
                QuizQuestion("In which sport do you roll a heavy ball at ten pins?", listOf("Bowling", "Curling", "Golf", "Billiards"), 0),
                QuizQuestion("A 3D shape whose every surface point is the same distance from its centre is a…", listOf("Cube", "Sphere", "Cone", "Cylinder"), 1),
                QuizQuestion("How many rings are on the Olympic flag?", listOf("Four", "Six", "Five", "Seven"), 2),
                QuizQuestion("A full turn around a circle measures how many degrees?", listOf("90°", "180°", "100°", "360°"), 3),
                QuizQuestion("Beginner jugglers usually start by juggling how many balls?", listOf("Three", "Five", "Seven", "Nine"), 0),
                QuizQuestion("In snooker and pool, what colour is the cue ball?", listOf("Black", "White", "Red", "Yellow"), 1)
            )
        ),
        QuizCategory(
            id = "round_medium",
            title = "Medium",
            subtitle = "Pi, planets and perfect spins",
            difficulty = 2,
            questions = listOf(
                QuizQuestion("The distance all the way around a circle is its…", listOf("Circumference", "Radius", "Area", "Sector"), 0),
                QuizQuestion("The number π (pi) is approximately…", listOf("2.72", "3.14", "1.62", "4.13"), 1),
                QuizQuestion("Which formula gives the area of a circle?", listOf("2πr", "πd", "πr²", "r² ÷ π"), 2),
                QuizQuestion("Which is the largest planet — a giant ball of gas?", listOf("Saturn", "Neptune", "Earth", "Jupiter"), 3),
                QuizQuestion("A straight line that touches a circle at exactly one point is a…", listOf("Tangent", "Secant", "Chord", "Radius"), 0),
                QuizQuestion("How many balls, including the cue ball, are used in a game of eight-ball pool?", listOf("15", "16", "10", "22"), 1),
                QuizQuestion("Earth is not a perfect sphere — it is slightly flattened at the…", listOf("Equator", "Oceans", "Poles", "Tropics"), 2),
                QuizQuestion("A \"pizza slice\" of a circle, between two radii, is called a…", listOf("Segment", "Chord", "Arc", "Sector"), 3),
                QuizQuestion("Roughly how many dimples does a typical golf ball have?", listOf("300 to 500", "About 50", "About 100", "Over 2,000"), 0),
                QuizQuestion("The steel balls thrown in the French game of pétanque are called…", listOf("Pucks", "Boules", "Quoits", "Marbles"), 1)
            )
        ),
        QuizCategory(
            id = "round_hard",
            title = "Hard",
            subtitle = "Radians, theorems and legends",
            difficulty = 3,
            questions = listOf(
                QuizQuestion("Which formula gives the volume of a sphere?", listOf("4πr²", "πr²h", "(4/3)πr³", "(1/3)πr²h"), 2),
                QuizQuestion("Which formula gives the surface area of a sphere?", listOf("2πr²", "πr²", "(4/3)πr²", "4πr²"), 3),
                QuizQuestion("A central angle is how big compared with an inscribed angle standing on the same arc?", listOf("Twice as big", "Equal", "Half as big", "Three times as big"), 0),
                QuizQuestion("One radian is the angle at which the arc length equals the…", listOf("Diameter", "Radius", "Circumference", "Chord"), 1),
                QuizQuestion("How many radians make a full circle?", listOf("π", "360", "2π", "π/2"), 2),
                QuizQuestion("Which ancient mathematician estimated π using 96-sided polygons?", listOf("Euclid", "Pythagoras", "Thales", "Archimedes"), 3),
                QuizQuestion("For a fixed perimeter, which shape encloses the largest area?", listOf("Circle", "Square", "Regular hexagon", "Equilateral triangle"), 0),
                QuizQuestion("Pi cannot be written as a fraction of two whole numbers, so it is…", listOf("Rational", "Irrational", "Negative", "A whole number"), 1),
                QuizQuestion("Since 1986, what colour are the tennis balls used at Wimbledon?", listOf("White", "Orange", "Yellow", "Green"), 2),
                QuizQuestion("The black-and-white Adidas \"Telstar\" first starred at which FIFA World Cup?", listOf("1958", "1982", "1994", "1970"), 3)
            )
        )
    )

    fun category(id: String?): QuizCategory? =
        if (id.isNullOrBlank()) null else categories.firstOrNull { it.id == id }
}
