package com.example.pip

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Preview
@Composable
fun GestureDemo(){
    val animatedOffsetX = remember { Animatable(0f) }
    val animatedOffsetY = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val density = LocalDensity.current
    val screenWidth = context.resources.displayMetrics.widthPixels
    val screenHeight = context.resources.displayMetrics.heightPixels
    val boxWidth = with(density) { 120.dp.toPx() }
    val boxHeight = with(density) { 200.dp.toPx() }

    var velocityX by remember { mutableStateOf(0f) }
    var velocityY by remember { mutableStateOf(0f) }
    var duration by remember { mutableStateOf(0L) }

    var initialVelocityEnabled by remember { mutableStateOf(true) }
    // 添加动画类型状态
    var isSpringAnimation by remember { mutableStateOf(true) }

    Box(
        modifier = androidx.compose.ui.Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                coroutineScope {
                    while (true) {
                        val startTime = System.currentTimeMillis()
                        val pointerId = awaitPointerEventScope { awaitFirstDown().id }
                        val velocityTracker = androidx.compose.ui.input.pointer.util.VelocityTracker()

                        awaitPointerEventScope {
                            drag(pointerId) {
                                launch {
                                    animatedOffsetX.snapTo(animatedOffsetX.value + it.positionChange().x)
                                    animatedOffsetY.snapTo(animatedOffsetY.value + it.positionChange().y)
                                }
                                velocityTracker.addPosition(it.uptimeMillis, it.position)
                            }
                        }

                        val velocity = velocityTracker.calculateVelocity()
                        velocityX = velocity.x
                        velocityY = velocity.y

                        val expectedX = animatedOffsetX.value + velocity.x * 0.1
                        val expectedY = animatedOffsetY.value + velocity.y * 0.1

                        val targetX = if (expectedX + boxWidth / 2 < screenWidth / 2) {
                            0f
                        } else {
                            (screenWidth - boxWidth)
                        }

                        val targetY = if (expectedY + boxHeight / 2 < screenHeight / 2) {
                            0f
                        } else {
                            (screenHeight - boxHeight)
                        }

                        coroutineScope.launch {
                            animatedOffsetX.animateTo(
                                targetValue = targetX,
                                animationSpec = if (isSpringAnimation) {
                                    spring(
                                        dampingRatio = 0.75f,
                                        stiffness = 300f
                                    )
                                } else {
                                    tween(
                                        500,
                                        easing = FastOutSlowInEasing
                                    )
                                },
                                initialVelocity = if (initialVelocityEnabled) velocity.x else 0f
                            )
                            duration = System.currentTimeMillis() - startTime
                        }

                        coroutineScope.launch {
                            animatedOffsetY.animateTo(
                                targetValue = targetY,
                                animationSpec = if (isSpringAnimation) {
                                    spring(
                                        dampingRatio = 0.7f,
                                        stiffness = 250f
                                    )
                                } else {
                                    tween(
                                        500,
                                        easing = FastOutSlowInEasing
                                    )
                                },
                                initialVelocity = if (initialVelocityEnabled) velocity.y else 0f
                            )
                            duration = System.currentTimeMillis() - startTime
                        }
                    }
                }
            }
    ) {
        Box(
            modifier = androidx.compose.ui.Modifier
                .offset {
                    IntOffset(
                        animatedOffsetX.value.roundToInt(),
                        animatedOffsetY.value.roundToInt()
                    )
                }
                .clip(RoundedCornerShape(12.dp))
                .width(120.dp)
                .height(200.dp)
                .background(Color(0xFF03A9F4))
        )

        Column(
            androidx.compose.ui.Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "VelocityX: ${velocityX.toInt()}\nVelocityY: ${velocityY.toInt()}\nDuration: ${duration}ms",
                color = Color.Black,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
            )

            Spacer(androidx.compose.ui.Modifier.height(15.dp))

            Button(onClick = { initialVelocityEnabled = !initialVelocityEnabled }) {
                Text(if (initialVelocityEnabled) "Turn off velocity" else "Turn on velocity")
            }

            Spacer(androidx.compose.ui.Modifier.height(15.dp))

            Button(onClick = { isSpringAnimation = !isSpringAnimation }) {
                Text(if (isSpringAnimation) "Switch to Tween" else "Switch to Spring")
            }
        }
    }
}