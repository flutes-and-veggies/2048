package com.packtpub.a2048

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import com.packtpub.a2048.composable.App
import com.packtpub.a2048.mobius.CellData
import com.packtpub.a2048.mobius.GameEffect
import com.packtpub.a2048.mobius.GameEffectHandler
import com.packtpub.a2048.mobius.GameEvent
import com.packtpub.a2048.mobius.GameModel
import com.packtpub.a2048.mobius.GameUpdate
import com.spotify.mobius.Mobius
import com.spotify.mobius.MobiusLoop
import org.json.JSONObject


class MainActivity : ComponentActivity() {
    private lateinit var loop: MobiusLoop<GameModel, GameEvent, GameEffect>

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
        val latestModel = loop.mostRecentModel
        if (latestModel == null) { return }
        with (sharedPref.edit()) {
            val json = JSONObject(latestModel.getBoardHashMap().mapValues { it.value.value }).toString()
            putString(BOARD_PREF_KEY, json)
            putInt(SCORE_PREF_KEY, latestModel.score)
            putInt(BEST_SCORE_PREF_KEY, latestModel.bestScore)
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

        loop.dispatchEvent(
            GameEvent.InitializeGameFromData(
                board = map,
                bestScore = bestScore,
                score = score
            )
        )

        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loop = Mobius
            .loop(GameUpdate(), GameEffectHandler())
            .startFrom(
                GameModel()
            )

        val restoredGame = readGameFromPreferences()
        if (!restoredGame) {
            loop.dispatchEvent(GameEvent.StartGame)
        }
        enableEdgeToEdge()

        val modelState = mutableStateOf<GameModel>(GameModel())

        loop.observe { model ->
            modelState.value = model
        }

        setContent {
            App(
                model = modelState.value,
                dispatchEvent = loop::dispatchEvent
            )
        }
    }
}