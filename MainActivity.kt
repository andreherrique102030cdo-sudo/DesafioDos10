package br.com.desafiodos10

import android.app.Activity
import android.os.Bundle
import android.os.CountDownTimer
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.math.abs
import kotlin.random.Random

class MainActivity : Activity() {

    private lateinit var root: LinearLayout
    private lateinit var title: TextView
    private lateinit var info: TextView
    private lateinit var question: TextView
    private lateinit var timerText: TextView
    private lateinit var scoreText: TextView
    private lateinit var options: LinearLayout
    private var timer: CountDownTimer? = null
    private var score = 0
    private var best = 0
    private var correctAnswer = 0
    private var running = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        best = getPreferences(0).getInt("best", 0)
        showMenu()
    }

    private fun baseLayout(): LinearLayout {
        val l = LinearLayout(this)
        l.orientation = LinearLayout.VERTICAL
        l.setPadding(32, 48, 32, 32)
        l.gravity = Gravity.CENTER_HORIZONTAL
        l.setBackgroundColor(Color.WHITE)
        return l
    }

    private fun text(value: String, size: Float, bold: Boolean = false): TextView {
        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(Color.rgb(25, 25, 25))
            gravity = Gravity.CENTER
            if (bold) typeface = Typeface.DEFAULT_BOLD
            setPadding(8, 12, 8, 12)
        }
    }

    private fun button(value: String, onClick: () -> Unit): Button {
        return Button(this).apply {
            text = value
            textSize = 18f
            isAllCaps = false
            setOnClickListener { onClick() }
            val p = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            p.setMargins(0, 10, 0, 10)
            layoutParams = p
        }
    }

    private fun showMenu() {
        timer?.cancel()
        running = false
        root = baseLayout()
        title = text("DESAFIO DOS 10", 32f, true)
        root.addView(title)
        root.addView(text("Você tem 10 segundos para responder.", 18f))
        root.addView(text("Acerte e avance. Errou ou acabou o tempo? O desafio termina.", 16f))
        root.addView(button("JOGAR", { startGame() }))
        root.addView(button("COMO JOGAR", { showHowToPlay() }))
        root.addView(text("Melhor pontuação: $best", 16f, true))
        setContentView(root)
    }

    private fun showHowToPlay() {
        root = baseLayout()
        root.addView(text("COMO JOGAR", 28f, true))
        root.addView(text(
            "1. Toque em JOGAR.\\n\\n" +
            "2. Resolva cada conta antes do fim dos 10 segundos.\\n\\n" +
            "3. Cada acerto vale 100 pontos.\\n\\n" +
            "4. Uma resposta errada ou o tempo zerado encerra a partida.\\n\\n" +
            "Versão 0.1 — modo offline.",
            18f
        ))
        root.addView(button("VOLTAR", { showMenu() }))
        setContentView(root)
    }

    private fun startGame() {
        score = 0
        running = true
        root = baseLayout()

        info = text("DESAFIO DOS 10", 26f, true)
        scoreText = text("Pontos: 0", 17f, true)
        timerText = text("10.0 s", 28f, true)
        question = text("", 36f, true)
        options = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
        }

        root.addView(info)
        root.addView(scoreText)
        root.addView(timerText)
        root.addView(question)
        root.addView(options)
        setContentView(root)
        nextQuestion()
    }

    private fun nextQuestion() {
        if (!running) return

        val a = Random.nextInt(1, 21)
        val b = Random.nextInt(1, 21)
        val op = Random.nextInt(3)

        correctAnswer = when (op) {
            0 -> a + b
            1 -> a - b
            else -> a * b
        }

        val symbol = when (op) {
            0 -> "+"
            1 -> "-"
            else -> "×"
        }

        question.text = "$a $symbol $b = ?"
        options.removeAllViews()

        val answers = mutableSetOf(correctAnswer)
        while (answers.size < 4) {
            val offset = Random.nextInt(-10, 11)
            val candidate = correctAnswer + if (offset == 0) 1 else offset
            answers.add(candidate)
        }

        answers.shuffled().forEach { answer ->
            options.addView(button(answer.toString()) {
                if (!running) return@button
                if (answer == correctAnswer) {
                    score += 100
                    scoreText.text = "Pontos: $score"
                    nextQuestion()
                } else {
                    endGame("Resposta errada!")
                }
            })
        }

        timer?.cancel()
        timer = object : CountDownTimer(10_000, 100) {
            override fun onTick(ms: Long) {
                timerText.text = String.format("%.1f s", ms / 1000.0)
            }

            override fun onFinish() {
                timerText.text = "0.0 s"
                endGame("Tempo esgotado!")
            }
        }.start()
    }

    private fun endGame(reason: String) {
        if (!running) return
        running = false
        timer?.cancel()

        if (score > best) {
            best = score
            getPreferences(0).edit().putInt("best", best).apply()
        }

        root = baseLayout()
        root.addView(text("FIM DE JOGO", 30f, true))
        root.addView(text(reason, 20f))
        root.addView(text("Sua pontuação", 17f))
        root.addView(text(score.toString(), 48f, true))
        root.addView(text("Melhor: $best", 18f, true))
        root.addView(button("JOGAR NOVAMENTE", { startGame() }))
        root.addView(button("MENU", { showMenu() }))
        setContentView(root)
    }

    override fun onDestroy() {
        timer?.cancel()
        super.onDestroy()
    }
}
