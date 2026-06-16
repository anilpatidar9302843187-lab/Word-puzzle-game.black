package com.example.data.model

data class GameLevel(
    val id: Int,
    val name: String,
    val letters: List<Char>,
    val targetWords: List<String>,
    val wordClues: Map<String, String> // High-engagement clue/meaning for dictionary popups
)

object LevelRegistry {
    val levels = listOf(
        GameLevel(
            id = 1,
            name = "Starlit Path",
            letters = listOf('A', 'E', 'R', 'T', 'S'),
            targetWords = listOf("ARE", "EAR", "EAT", "RAT", "TEA", "EAST", "RATE", "STAR", "TEAR", "STARE", "TEARS"),
            wordClues = mapOf(
                "ARE" to "To exist or live (plural form of 'to be')",
                "EAR" to "The organ of hearing",
                "EAT" to "To take food into the mouth and swallow it",
                "RAT" to "A smart, long-tailed rodent larger than a mouse",
                "TEA" to "A hot drink made from dried plant leaves",
                "EAST" to "The direction where the sun rises",
                "RATE" to "The speed at which something happens or changes",
                "STAR" to "A brilliant glowing point of light in the night sky",
                "TEAR" to "A drop of clear salty fluid secreted by the eye",
                "STARE" to "To look fixedly or persistently with wide-open eyes",
                "TEARS" to "Drops of salty water produced by crying"
            )
        ),
        GameLevel(
            id = 2,
            name = "Cozy Harbor",
            letters = listOf('O', 'P', 'E', 'N', 'S'),
            targetWords = listOf("ONE", "PEN", "SON", "SOP", "OPEN", "PENS", "POSE", "NOSE", "ONES", "OPENS"),
            wordClues = mapOf(
                "ONE" to "The lowest cardinal number; single",
                "PEN" to "An instrument for writing with ink",
                "SON" to "A male child in relation to his parents",
                "SOP" to "A piece of bread dipped in liquid, or to soak up",
                "OPEN" to "Not shut or closed; allowing passage",
                "PENS" to "Writing instruments, or small enclosures for animals",
                "POSE" to "To assume a particular stance, especially for a photo",
                "NOSE" to "The part of the face containing the nostrils",
                "ONES" to "Plural of one; single individuals or items",
                "OPENS" to "Unlocks or makes accessible; unfolds"
            )
        ),
        GameLevel(
            id = 3,
            name = "Forest Cleat",
            letters = listOf('C', 'A', 'T', 'E', 'L'),
            targetWords = listOf("ACT", "ALE", "ATE", "CAT", "EAT", "LET", "TEA", "LACE", "LATE", "TALE", "TEAL", "CLEAT"),
            wordClues = mapOf(
                "ACT" to "To perform an action or behave in a specified way",
                "ALE" to "A type of beer brewed using a warm fermentation method",
                "ATE" to "The past tense of eat; consumed food",
                "CAT" to "A small domesticated carnivorous mammal kept as a pet",
                "EAT" to "Consume food",
                "LET" to "Allow or permit",
                "TEA" to "Infused beverage of leaf buds",
                "LACE" to "A fine open fabric of cotton or silk in patterns",
                "LATE" to "Doing something or arriving after the expected time",
                "TALE" to "A fictitious or true narrative; a story",
                "TEAL" to "A medium-sized duck, or a dark greenish-blue color",
                "CLEAT" to "A strip of wood or metal fastened to prevent slipping"
            )
        ),
        GameLevel(
            id = 4,
            name = "Fresh Hearth",
            letters = listOf('B', 'R', 'E', 'A', 'D'),
            targetWords = listOf("BAD", "BAR", "BED", "RED", "EAR", "ERA", "DAB", "BARE", "BEAR", "DEAR", "DARE", "READ", "DRAB", "BREAD", "BEARD"),
            wordClues = mapOf(
                "BAD" to "Not good; of poor quality or unfavorable",
                "BAR" to "A long solid piece of metal or wood, or a counter drink establishment",
                "BED" to "A piece of furniture for sleep or rest",
                "RED" to "The color of blood, fire, or ripe tomatoes",
                "EAR" to "Hearing organ",
                "ERA" to "A distinct period of history with a particular feature",
                "DAB" to "To press gently with a soft hand or sponge",
                "BARE" to "Naked, uncovered, or simple",
                "BEAR" to "A heavy mammal with thick fur, or to carry weight",
                "DEAR" to "Regarded with deep affection; expensive",
                "DARE" to "Have the courage to do something challenging",
                "READ" to "Look at and comprehend the meaning of written characters",
                "DRAB" to "Lacking brightness or interest; drearily dull",
                "BREAD" to "Food made of flour, water, and yeast mixed and baked",
                "BEARD" to "A growth of hair on the chin and lower cheeks of a man's face"
            )
        ),
        GameLevel(
            id = 5,
            name = "Verdant Spire",
            letters = listOf('S', 'P', 'I', 'N', 'E'),
            targetWords = listOf("PIN", "PEN", "NIP", "SIN", "SIP", "PIE", "SPIN", "PINE", "NIPS", "PENS", "SINE", "SNIP", "SPINE"),
            wordClues = mapOf(
                "PIN" to "A thin piece of metal used to fasten things",
                "PEN" to "Writing instrument",
                "NIP" to "To pinch or squeeze sharply",
                "SIN" to "An immoral act considered to be a transgression",
                "SIP" to "Drink in small mouthfuls",
                "PIE" to "A baked dish of fruit, meat, or vegetables with pastry",
                "SPIN" to "Turn round and round quickly",
                "PINE" to "An evergreen coniferous tree, or to yearn deeply",
                "NIPS" to "Sharp squeezes or small bites",
                "PENS" to "Enclosures, or plural of writing pens",
                "SINE" to "A trigonometric function of an angle",
                "SNIP" to "Cut with scissors in a single quick stroke",
                "SPINE" to "A series of vertebrae extending from the skull to the pelvis"
            )
        )
    )

    fun getById(id: Int): GameLevel {
        return levels.firstOrNull { it.id == id } ?: levels.first()
    }
}
