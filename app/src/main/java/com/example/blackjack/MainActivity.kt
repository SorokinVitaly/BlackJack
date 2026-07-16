package com.example.blackjack

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state by viewModel.state.collectAsStateWithLifecycle()
            MainScreen(state)
        }
    }

    @Composable
    fun MainScreen(state: ScreenState) {
        val context = LocalContext.current
        LaunchedEffect(Unit) {
            viewModel.events.collect { event ->
                when (event) {
                    is UiEvent.ShowToast -> {
                        Toast.makeText(
                            context,
                            event.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

        Surface(
            color = colorResource(R.color.backGround),
            modifier = Modifier
                .navigationBarsPadding()
                .fillMaxSize()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                ConstraintLayout(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    val margin = 20.dp
                    Player(
                        cards = state.dealerCards,
                        chips = 0,
                        points = state.dealerPoints,
                        isDealer = true,
                        modifier = Modifier.constrainAs(createRef()
                        ) {
                            centerHorizontallyTo(parent)
                            top.linkTo(parent.top, margin = margin)
                        }
                    )
                    Player(
                        cards = state.playerCards,
                        chips = state.playerChips,
                        points = state.playerPoints,
                        isDealer = false,
                        modifier = Modifier.constrainAs(createRef()
                        ) {
                            centerHorizontallyTo(parent)
                            bottom.linkTo(parent.bottom, margin = margin)
                        }
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    ActionBar(state)
                }
            }
        }
    }

    @Composable
    fun ActionBar(state: ScreenState) {
        if (state.isActionAvailable) {
            if (state.isGameStarted) {
                AppButton("Hit", viewModel::onHit)
                AppButton("Stand", viewModel::onStand)
            } else {
                if (state.playerChips > 0) {
                    AppButton("Deal next", viewModel::onDealNext)
                }
                if (state.isResetAvailable) {
                    AppButton("Reset game", viewModel::onResetGame)
                }
            }
        } else {
            CircularProgressIndicator(
                color = Color.Yellow,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}