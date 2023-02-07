package com.plcoding.stockmarketapp.presentation.company_info

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.plcoding.stockmarketapp.domain.model.IntradayInfo
import kotlin.math.round
import kotlin.math.roundToInt

@Composable
fun StockChart(
    infos: List<IntradayInfo> = emptyList(),
    modifier: Modifier = Modifier,
    graphColor: Color = Color.Green
) {
    val spacing = 100f // space for numbers on y and x axis
    val transparentGraphColor = remember {
        // farba pod krivkou v grafe zacina syta a ide do stratenia
        graphColor.copy(alpha = 0.5f)
    }
    val upperValue = remember(infos) { // remember function = to remember values on recomposition,
                                        // infos aby sa upperValue prepocitala ak sa hodnota infos zmeni
        // horna hranica hodnoty na burze
        // aby sa dala vytvorit zvisla stupnica v canvase s medzi hodnotami
        (infos.maxOfOrNull { it.close }?.plus(1))?.roundToInt() ?: 0
        // MAX z listu hodnoty close
    }
    val lowerValue = remember(infos) {
        // dolna hranica hodnoty na burze
        infos.minOfOrNull { it.close }?.toInt() ?: 0
    }
    val density = LocalDensity.current
    val textPaint = remember(density) { // na kreslenie textu na screen
        // android.graphics nie compose
        Paint().apply {
            color = android.graphics.Color.WHITE
            textAlign = Paint.Align.CENTER
            textSize = density.run { 12.sp.toPx() } // aby sme zadali spravnu velkost v sp musime ju dat v px a pomocou density
        }
    }

    Canvas(modifier = modifier) {
        // vykreslenie x suradnice s hodinami
        val spacePerHour = (size.width - spacing) / infos.size // medzera medzi cislami hodin pod grafom
        (0 until infos.size - 1 step 2).forEach { i ->
            // vykreslit kazdu druhu hodinu 5   7   9 ...
            val info = infos[i]
            val hour = info.date.hour
            drawContext.canvas.nativeCanvas.apply {
                // iba v native canvas mame pristup k kresleniu textu
                drawText(
                    hour.toString(),
                    spacing + i * spacePerHour,
                    size.height - 5,
                    textPaint
                )
            }
        }

        // vykreslenie y suradnice s hodnotami stock
        val priceStep = (upperValue - lowerValue) / 5f // zobrazit chceme len 5 hodnot aby boli v intervale MAx Min
        (0..4).forEach { i ->
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    round(lowerValue + priceStep * i).toString(), // hodnota bude postupne narastat od min po max
                    30f,
                    size.height - spacing - i * size.height / 5f, // y pozicia pojde vyssie kde bude najvyssia pozicia
                    textPaint
                )
            }
        }


        var lastX = 0f
        // Path je kreslenie ciar alebo len presuvanie sa bez kreslenia, zacina v lavom hornom rohu na x0:y0
        val strokePath = Path().apply {
            val height = size.height
            for(i in infos.indices) {
                val info = infos[i]
                val nextInfo = infos.getOrNull(i + 1) ?: infos.last()
                val leftRatio = (info.close - lowerValue) / (upperValue - lowerValue)
                val rightRatio = (nextInfo.close - lowerValue) / (upperValue - lowerValue)

                val x1 = spacing + i * spacePerHour
                val y1 = height - spacing - (leftRatio * height).toFloat()
                val x2 = spacing + (i + 1) * spacePerHour
                val y2 = height - spacing - (rightRatio * height).toFloat()
                if(i == 0) {
                    moveTo(x1, y1)
                }
                lastX = (x1 + x2) / 2f
                quadraticBezierTo( // oblejsie zakrivenie
                    x1, y1, lastX, (y1 + y2) / 2f
                )
            }
        }

        // android.graphics.Path aby sme mohli kopirovat predchadzajuci Path
        val fillPath = android.graphics.Path(strokePath.asAndroidPath())
            .asComposePath()
            .apply {
                lineTo(lastX, size.height - spacing)
                lineTo(spacing, size.height - spacing)
                close() // connect end point with starting point
            }

        // Fill path treba vykreslit prvu
        // fill path should be behind the stroke path so it doesnt hide any parts of the stroke
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    transparentGraphColor,
                    Color.Transparent
                ),
                endY = size.height - spacing
            )
        )
        drawPath(
            path = strokePath,
            color = graphColor,
            style = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        )
    }
}