package com.codeword.app.feature.game

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codeword.app.core.model.Card
import com.codeword.app.core.model.CardColor
import com.codeword.app.core.model.Role
import com.codeword.app.ui.theme.CardAssassin
import com.codeword.app.ui.theme.CardAssassinLight
import com.codeword.app.ui.theme.CardBlue
import com.codeword.app.ui.theme.CardBlueLight
import com.codeword.app.ui.theme.CardNeutral
import com.codeword.app.ui.theme.CardNeutralLight
import com.codeword.app.ui.theme.CardRed
import com.codeword.app.ui.theme.CardRedLight
import com.codeword.app.ui.theme.CardUnrevealed
import com.codeword.app.ui.theme.CardUnrevealedText

@Composable
fun CardView(
    card: Card,
    myRole: Role,
    isEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rotation by animateFloatAsState(
        targetValue = if (card.revealed) 180f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "flip_${card.id}",
    )
    val isFront = rotation <= 90f

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            },
    ) {
        if (isFront) {
            CardFace(
                backgroundColor = card.color.frontColor(myRole),
                textColor = card.color.frontTextColor(myRole),
                word = card.word,
                clickable = isEnabled && !card.revealed,
                onClick = onClick,
            )
        } else {
            // counter-rotate so text reads normally on the back face
            CardFace(
                backgroundColor = card.color.revealedColor(),
                textColor = card.color.revealedTextColor(),
                word = card.word,
                clickable = false,
                onClick = {},
                modifier = Modifier.graphicsLayer { rotationY = 180f },
            )
        }
    }
}

@Composable
private fun CardFace(
    backgroundColor: Color,
    textColor: Color,
    word: String,
    clickable: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .then(if (clickable) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = word,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// Unrevealed card color — spymaster sees hint, operative sees nothing
private fun CardColor.frontColor(myRole: Role): Color = when (myRole) {
    Role.SPYMASTER -> when (this) {
        CardColor.RED -> CardRedLight
        CardColor.BLUE -> CardBlueLight
        CardColor.NEUTRAL -> CardNeutralLight
        CardColor.ASSASSIN -> CardAssassinLight
    }
    Role.OPERATIVE, Role.SPECTATOR -> CardUnrevealed
}

private fun CardColor.frontTextColor(myRole: Role): Color = when (myRole) {
    Role.SPYMASTER -> when (this) {
        CardColor.ASSASSIN -> Color.White
        else -> CardUnrevealedText
    }
    Role.OPERATIVE, Role.SPECTATOR -> CardUnrevealedText
}

// Revealed card color — full saturation, same for both roles
private fun CardColor.revealedColor(): Color = when (this) {
    CardColor.RED -> CardRed
    CardColor.BLUE -> CardBlue
    CardColor.NEUTRAL -> CardNeutral
    CardColor.ASSASSIN -> CardAssassin
}

private fun CardColor.revealedTextColor(): Color = when (this) {
    CardColor.NEUTRAL -> CardUnrevealedText
    else -> Color.White
}
