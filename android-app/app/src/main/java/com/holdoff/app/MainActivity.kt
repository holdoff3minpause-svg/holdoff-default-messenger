package com.holdoff.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.holdoff.app.sms.Conversation
import com.holdoff.app.sms.MessageRepository
import com.holdoff.app.sms.SmsRoleManager
import com.holdoff.app.sms.SmsSender
import com.holdoff.app.ui.theme.HoldOffTheme

/** Native, role-based HoldOff messenger. Support features are optional layers, never an SMS substitute. */
class MainActivity : ComponentActivity() {
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { HoldOffTheme { MessengerApp() } }
    }

    private fun requestMessagingPermissions() {
        val needed = arrayOf(Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS, Manifest.permission.SEND_SMS, Manifest.permission.READ_CONTACTS)
            .filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
        if (needed.isNotEmpty()) permissionLauncher.launch(needed.toTypedArray())
    }

    @androidx.compose.runtime.Composable
    private fun MessengerApp() {
        var selected by remember { mutableStateOf<Conversation?>(null) }
        var refresh by remember { mutableStateOf(0) }
        val roleHeld = SmsRoleManager.isDefaultSmsApp(this)
        val threads = remember(refresh, roleHeld) {
            if (roleHeld && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) MessageRepository(this).conversations() else emptyList()
        }
        if (selected == null) {
            Column(Modifier.fillMaxSize().padding(20.dp)) {
                Text("HoldOff", style = MaterialTheme.typography.headlineLarge)
                Text("Your messages, with room to pause before you send.", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(16.dp))
                if (!roleHeld) {
                    Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) {
                        Text("Set up HoldOff as your default messaging app", style = MaterialTheme.typography.titleMedium)
                        Text("This asks Android for the SMS role. Until you choose it, HoldOff stays in limited manual mode.")
                        Spacer(Modifier.height(10.dp))
                        Button(onClick = { requestMessagingPermissions(); SmsRoleManager.request(this@MainActivity) }) { Text("Set up messaging") }
                    } }
                } else {
                    Button(onClick = { refresh++ }) { Text("Refresh conversations") }
                    Spacer(Modifier.height(8.dp))
                    if (threads.isEmpty()) Text("No SMS conversations yet, or SMS permission was not granted.")
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(threads, key = { it.threadId }) { thread ->
                            Card(Modifier.fillMaxWidth().clickable { selected = thread }) { Column(Modifier.padding(14.dp)) {
                                Text(thread.address.ifBlank { "Unknown sender" }, style = MaterialTheme.typography.titleMedium)
                                Text(thread.snippet, maxLines = 1)
                            } }
                        }
                    }
                }
            }
        } else {
            ThreadScreen(selected!!, onBack = { selected = null }, onSent = { refresh++ })
        }
    }

    @androidx.compose.runtime.Composable
    private fun ThreadScreen(thread: Conversation, onBack: () -> Unit, onSent: () -> Unit) {
        var draft by remember { mutableStateOf("") }
        var status by remember { mutableStateOf("") }
        val messages = remember(thread.threadId, status) { MessageRepository(this).messages(thread.threadId) }
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Row { Button(onClick = onBack) { Text("Back") }; Spacer(Modifier.padding(6.dp)); Text(thread.address, style = MaterialTheme.typography.titleLarge) }
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(messages, key = { it.id }) { message ->
                    Text(if (message.outgoing) "You: ${message.body}" else "${thread.address}: ${message.body}")
                }
            }
            Text("Sadie can help you reflect if you choose. She is not therapy or emergency care.", style = MaterialTheme.typography.bodySmall)
            OutlinedTextField(value = draft, onValueChange = { draft = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Message") })
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { status = if (draft.isBlank()) "Write a message to save it as held." else { com.holdoff.app.sms.DraftStore(this@MainActivity).hold(thread.address, draft); "Held locally. Nothing was sent." } }) { Text("Hold") }
                Button(onClick = {
                    SmsSender(this@MainActivity).send(thread.address, draft).onSuccess { draft = ""; status = "Sent"; onSent() }.onFailure { status = it.message ?: "Could not send." }
                }) { Text("Send") }
            }
            if (status.isNotBlank()) Text(status)
        }
    }
}
