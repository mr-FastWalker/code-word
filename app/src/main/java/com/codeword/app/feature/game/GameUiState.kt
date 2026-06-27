package com.codeword.app.feature.game

import com.codeword.app.core.model.Card
import com.codeword.app.core.model.Clue
import com.codeword.app.core.model.GamePhase
import com.codeword.app.core.model.Role
import com.codeword.app.core.model.Score
import com.codeword.app.core.model.Team
import com.codeword.app.core.model.WinReason

data class GameUiState(
    val board: List<Card>,
    val currentTeam: Team,
    val phase: GamePhase,
    val clue: Clue?,
    // Int.MAX_VALUE = unlimited (clue count == 0)
    val guessesLeft: Int,
    val score: Score,
    val winner: Team?,
    val winReason: WinReason?,
    val myRole: Role,
    val myTeam: Team,
) {
    val isSpectator: Boolean get() = myRole == Role.SPECTATOR
    val isMyTurn: Boolean get() = !isSpectator && currentTeam == myTeam
    val isActiveSpymaster: Boolean get() = isMyTurn && myRole == Role.SPYMASTER && phase == GamePhase.CLUE
    val isActiveOperative: Boolean get() = isMyTurn && myRole == Role.OPERATIVE && phase == GamePhase.GUESS

    // Сколько карточек команды осталось — это и есть потолок для count подсказки
    val myTeamCardsLeft: Int get() = if (myTeam == Team.RED) score.redLeft else score.blueLeft
}
