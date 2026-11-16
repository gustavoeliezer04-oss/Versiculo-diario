package br.com.gustavoeliezer.versiculodiario

data class Verse(val reference: String, val text: String)

object VerseLibrary {
    // Textos em linguagem própria (paráfrases), não vinculados a uma tradução bíblica comercial.
    val all = listOf(
        Verse("Salmos 23:1", "O Senhor cuida de mim; por isso, não me falta o necessário."),
        Verse("Provérbios 3:5", "Confie no Senhor de todo o coração e não dependa apenas do próprio entendimento."),
        Verse("Filipenses 4:6-7", "Não viva dominado pela ansiedade. Apresente suas preocupações a Deus em oração."),
        Verse("Isaías 41:10", "Não tenha medo: Deus está com você, fortalece e sustenta você."),
        Verse("Mateus 11:28", "Venham a mim os que estão cansados e sobrecarregados, e encontrarão descanso."),
        Verse("Salmos 46:1", "Deus é abrigo e força, ajuda sempre presente nos momentos difíceis."),
        Verse("Romanos 12:12", "Mantenha a esperança, tenha paciência nas dificuldades e persevere em oração.")
    )
}
