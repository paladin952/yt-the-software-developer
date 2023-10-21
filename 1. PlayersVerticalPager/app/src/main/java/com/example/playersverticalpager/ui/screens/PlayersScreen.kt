package com.example.playersverticalpager.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import com.example.playersverticalpager.ui.components.PlayerComponent
import com.example.playersverticalpager.ui.data.MockedDataProvider
import com.example.playersverticalpager.ui.data.PlayersUiModel


@Composable
fun PlayersScreen(data: List<PlayersUiModel> = MockedDataProvider.mockedPlayers) {
    Content(data)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Content(players: List<PlayersUiModel>) {

    // 3D effect region
    val pagerState = rememberPagerState()

    val sliderRange: ClosedFloatingPointRange<Float> = remember { 1f..25f }
    val sliderValue =
        remember { mutableStateOf((sliderRange.start + sliderRange.endInclusive) / 2) }
    val density = remember {
        mutableStateOf(2.75f)
    }
    val transformOriginBottom = remember {
        TransformOrigin(0.5f, 0f)
    }

    val transformOriginTop = remember {
        TransformOrigin(0.5f, 1f)
    }
    // end region

    val currentPageOffset = remember(pagerState) {
        derivedStateOf {
            if (pagerState.currentPageOffsetFraction == 0f) 0f else -1
        }
    }

    val currentPageIndex = remember {
        mutableStateOf(pagerState.currentPage)
    }

    LaunchedEffect(pagerState.currentPage, currentPageOffset.value) {
        if (currentPageOffset.value == 0f) {
            currentPageIndex.value = pagerState.currentPage
        }
    }

    Box(
        modifier = Modifier
            .graphicsLayer {
            density.value = this.density
        }
    ) {
        VerticalPager(
            modifier = Modifier.safeDrawingPadding(),
            pageCount = players.size,
            state = pagerState
        ) { pageIndex ->
            val pageOffset =
                (pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction
            val isActivePage = pageIndex == currentPageIndex.value

            Page(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .animation3dGraphicsLayer(
                        isActivePage = isActivePage,
                        sliderValue = sliderValue,
                        density = density,
                        angle = if (pageOffset < 0) pageOffset * -1 * 90f else pageOffset * 90f,
                        pageOffset = pageOffset,
                        transformOriginBottom = transformOriginBottom,
                        transformOriginTop = transformOriginTop
                    ),
                playersUiModel = players[pageIndex]
            )
        }
    }
}

@Composable
fun Page(modifier: Modifier = Modifier, playersUiModel: PlayersUiModel) {
    Box(modifier = modifier) {
        PlayerComponent(playersUiModel = playersUiModel)
    }
}

internal fun Modifier.animation3dGraphicsLayer(
    isActivePage: Boolean,
    sliderValue: State<Float>,
    density: MutableState<Float>,
    angle: Float,
    pageOffset: Float,
    transformOriginBottom: TransformOrigin,
    transformOriginTop: TransformOrigin,
): Modifier =
    this.graphicsLayer {
        val isSwipingDown = isSwipingDown(isActivePage, pageOffset)

        cameraDistance = sliderValue.value * density.value
        clip = true

        if (isActivePage) {
            if (isSwipingDown) {
                transformOrigin = transformOriginBottom
                rotationX = -1 * angle
            } else {
                transformOrigin = transformOriginTop
                rotationX = angle
            }
        } else {
            if (isSwipingDown) {
                transformOrigin = transformOriginTop
                rotationX = angle
            } else {
                transformOrigin = transformOriginBottom
                rotationX = -1 * angle
            }
        }
    }

internal fun isSwipingDown(isActivePage: Boolean, pageOffset: Float): Boolean =
    if (isActivePage) pageOffset < 0 else pageOffset > 0
