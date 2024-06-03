@file:OptIn(ExperimentalMaterial3Api::class)

package com.laizhenghuo.binary_execute

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.laizhenghuo.binary_execute.ui.theme.BinaryexecuteTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val applicationScope = CoroutineScope(SupervisorJob())
        applicationScope.launch((Dispatchers.IO)) {
            try {
                BinaryExecute.init(this@MainActivity)
//                BinaryExecute.getInstance().execute("https://docs.python.org/zh-cn/3.8//library/zipapp.html")
            } catch (e: Exception) {
                println(e)
            }
        }
        setContent {
            BinaryexecuteTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Content()
                }
            }
        }
    }
}

@Composable
fun Content(modifier: Modifier = Modifier) {
    val textState = rememberSaveable {
        mutableStateOf("")
    }

    val result = rememberSaveable {
        mutableStateOf("")
    }


    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        OutlinedTextField(value = textState.value, onValueChange = {
            textState.value = it
        })
        Spacer(modifier = Modifier.height(20.dp))
        TextButton(onClick = {
            val applicationScope = CoroutineScope(SupervisorJob())
            applicationScope.launch((Dispatchers.IO)) {
                try {
                    result.value = BinaryExecute.getInstance().execute(textState.value)
                    println(result.value)
                } catch (e: Exception) {
                    println(e)
                }
            }
        }) {
            Text(text = "访问链接")
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = result.value,
            maxLines = 16,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            modifier = modifier
        )
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BinaryexecuteTheme {
        Content()
    }
}