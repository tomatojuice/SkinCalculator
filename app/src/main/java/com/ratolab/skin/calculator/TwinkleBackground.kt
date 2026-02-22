package com.ratolab.skin.calculator

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import kotlin.random.Random

// キラキラの粒の状態を管理するクラス
// 値が変化した時に再描画されるよう、MutableStateを使用します
class TwinkleParticleState {
    // 位置（0.0〜1.0の割合）
    var xPercent by mutableFloatStateOf(0f)
    var yPercent by mutableFloatStateOf(0f)
    // 半径と目標の最大透明度
    var radius by mutableFloatStateOf(0f)
    var targetAlpha by mutableFloatStateOf(0f)

    // 透明度のアニメーション用（初期値は0で見えない状態）
    val alphaAnimatable = Animatable(0f)

    // パラメータをランダムにリセットする関数（新しい場所に現れる準備）
    fun reset() {
        xPercent = Random.nextFloat()
        yPercent = Random.nextFloat()
        radius = Random.nextFloat() * 15f + 1f // 大きさの範囲
        targetAlpha = Random.nextFloat() * 0.6f + 0.4f // 最大の明るさ（少し明るめに調整）
    }
}

@Composable
fun TwinkleBackground(
    modifier: Modifier = Modifier,
    particleCount: Int = 60, // キラキラの数
    color: Color = Color.White // 色
) {
    // パーティクルの状態リストを作成
    val particles = remember {
        List(particleCount) { TwinkleParticleState() }
    }

    // 各パーティクルごとに独立したアニメーションループを起動
    particles.forEachIndexed { index, particle ->
        // keyにindexを指定して、それぞれの粒で個別のコルーチンが動くようにする
        LaunchedEffect(key1 = index) {
            // 最初にランダムな時間だけ待機して、光り始めるタイミングをばらつかせる
            delay(Random.nextLong(0, 2000))

            // 無限ループで「光って消える」を繰り返す
            while (true) {
                // 1. 位置とパラメータをリセット（ここで新しい場所が決まる！）
                particle.reset()
                // 透明度を一旦、最小値（ほぼ見えない状態）に瞬時にセット
                particle.alphaAnimatable.snapTo(particle.targetAlpha * 0.1f)

                // 1回の点滅にかかる時間をランダムに決定
                val duration = Random.nextInt(1500, 3000)
                // じわっと明るくなる/暗くなるためのイージング（変化のカーブ）
                val easing = FastOutSlowInEasing

                // 2. 明るくなるアニメーション (Fade In)
                // animateTo はアニメーションが終わるまでここで処理を一時停止します
                particle.alphaAnimatable.animateTo(
                    targetValue = particle.targetAlpha,
                    animationSpec = tween(durationMillis = duration, easing = easing)
                )

                // 3. 暗くなるアニメーション (Fade Out)
                particle.alphaAnimatable.animateTo(
                    targetValue = particle.targetAlpha * 0.1f,
                    animationSpec = tween(durationMillis = duration, easing = easing)
                )

                // 4. 次に現れるまで少し待機（間を持たせる）
                delay(Random.nextLong(200, 1000))

                // → ループの先頭に戻り、reset()でまた別の場所に現れる
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        particles.forEach { particle ->
            // 現在の透明度が少しでもある場合のみ描画
            if (particle.alphaAnimatable.value > 0.01f) {
                drawCircle(
                    color = color,
                    radius = particle.radius,
                    // アニメーション中の現在の透明度を使用
                    alpha = particle.alphaAnimatable.value,
                    // reset()で設定された最新の位置に描画
                    center = Offset(size.width * particle.xPercent, size.height * particle.yPercent)
                )
            }
        }
    }
}