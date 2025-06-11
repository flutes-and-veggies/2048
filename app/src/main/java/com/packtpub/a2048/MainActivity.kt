package com.packtpub.a2048

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.packtpub.a2048.composable.App
import com.packtpub.a2048.mobius.GameEffect
import com.packtpub.a2048.mobius.GameEffectHandler
import com.packtpub.a2048.mobius.GameEvent
import com.packtpub.a2048.mobius.GameModel
import com.packtpub.a2048.mobius.GameUpdate
import com.packtpub.a2048.viewmodel.CellData
import com.packtpub.a2048.viewmodel.GameViewModel
import com.spotify.mobius.Mobius
import com.spotify.mobius.MobiusLoop
import org.json.JSONObject


class MainActivity : ComponentActivity() {
    private lateinit var viewModel: GameViewModel

    companion object {
        const val SECONDARY_AXIS_THRESHOLD = 40
        const val MAIN_AXIS_THRESHOLD = 30
        const val BOARD_PREF_KEY = "BOARD_PREF_KEY"
        const val SCORE_PREF_KEY = "SCORE_PREF_KEY"
        const val BEST_SCORE_PREF_KEY = "BEST_SCORE_PREF_KEY"
    }

    override fun onStop() {
        super.onStop()
        saveGameToPreferences()
    }

    private fun saveGameToPreferences() {
        val sharedPref = getPreferences(MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            val json = JSONObject(viewModel.getBoardHashMap().mapValues { it.value.value }).toString()
            putString(BOARD_PREF_KEY, json)
            putInt(SCORE_PREF_KEY, viewModel.scoreFlow.value)
            putInt(BEST_SCORE_PREF_KEY, viewModel.bestScoreFlow.value)
            apply()
        }
    }

    private fun readGameFromPreferences(): Boolean {
        val sharedPref = getPreferences(MODE_PRIVATE) ?: return false
        val boardJson = sharedPref.getString(BOARD_PREF_KEY, null) ?: return false
        val score = sharedPref.getInt(SCORE_PREF_KEY, 0)
        val bestScore = sharedPref.getInt(BEST_SCORE_PREF_KEY, 0)
        val boardJsonObj = JSONObject(boardJson)
        val map = mutableMapOf<Pair<Int, Int>, CellData>()

        for (key in boardJsonObj.keys()) {
            val (x, y) = key.split(", ").map { it.toInt() }
            map[Pair(x, y)] = CellData(value = boardJsonObj.getInt(key))
        }
        viewModel.initializeBoard()
        viewModel.setNewBoard(map)
        viewModel.setBestScore(bestScore)
        viewModel.setScore(score, showAddedScore = false)

        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = GameViewModel(4, 4)

        val loop: MobiusLoop<GameModel, GameEvent, GameEffect> = Mobius
            .loop(GameUpdate(), GameEffectHandler())
            .startFrom(
                GameModel()
            )

        val restoredGame = readGameFromPreferences()
        if (!restoredGame) {
            viewModel.startGame()
        }
        viewModel.printBoard()
        enableEdgeToEdge()

        setContent {
            App(viewModel, loop::dispatchEvent)
        }
    }
}