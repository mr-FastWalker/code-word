package com.codeword.app.feature.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.codeword.app.core.model.Card
import com.codeword.app.core.model.Role

@Composable
fun BoardGrid(
    board: List<Card>,
    myRole: Role,
    isActiveOperative: Boolean,
    onCardTap: (Card) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        modifier = modifier,
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        userScrollEnabled = false,
    ) {
        items(board, key = { it.id }) { card ->
            CardView(
                card = card,
                myRole = myRole,
                isEnabled = isActiveOperative && !card.revealed,
                onClick = { onCardTap(card) },
            )
        }
    }
}
