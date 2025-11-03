package com.guruprasad.studysmartandroidapp.domain.model

import androidx.compose.ui.graphics.Color
import com.example.compose.gradient1
import com.example.compose.gradient2
import com.example.compose.gradient3
import com.example.compose.gradient4
import com.example.compose.gradient5

data class Subject(
    val id : Int,
    val name : String,
    val goalHours : Float,
    val colors : List<Color>
){
    companion object{
        val subjectCardColors = listOf(gradient1, gradient2, gradient3, gradient4, gradient5)
    }
}
