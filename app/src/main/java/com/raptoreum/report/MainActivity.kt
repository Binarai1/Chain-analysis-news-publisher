package com.raptoreum.report

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

class MainActivity : ComponentActivity() {

    private val viewModel: ReportViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                var showReport by remember { mutableStateOf(false) }

                Scaffold { padding ->
                    ReportScreen(
                        uiState = uiState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        onStartScan = { viewModel.startScrape() },
                        onViewReport = { showReport = true },
                        onSave = { viewModel.saveReport(this) },
                        onShare = { target -> viewModel.shareText(this, target) },
                        onSharePdf = { target -> viewModel.sharePdf(this, target) }
                    )
                }

                if (showReport && uiState.report != null) {
                    ReportDialog(report = uiState.report!!) {
                        showReport = false
                    }
                }
            }
        }
    }
}

@Composable
fun ReportScreen(
    uiState: ReportUiState,
    modifier: Modifier = Modifier,
    onStartScan: () -> Unit,
    onViewReport: () -> Unit,
    onSave: () -> Unit,
    onShare: (ShareTarget) -> Unit,
    onSharePdf: (ShareTarget) -> Unit
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Raptoreum Intelligence Scanner", style = MaterialTheme.typography.headlineSmall)
        Text(text = "Scrapes blockchain, market, social, and video signals across the open web.", style = MaterialTheme.typography.bodyMedium)

        StatusPanel(uiState)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onStartScan, enabled = !uiState.isRunning) {
                Icon(Icons.Default.Refresh, contentDescription = "Start")
                Text(text = "Start Scan", modifier = Modifier.padding(start = 6.dp))
            }
            Button(onClick = onViewReport, enabled = uiState.report != null) {
                Icon(Icons.Default.Article, contentDescription = "View")
                Text(text = "View Report", modifier = Modifier.padding(start = 6.dp))
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onSave, enabled = uiState.report != null) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = "Save PDF")
                Text(text = "Save as PDF", modifier = Modifier.padding(start = 6.dp))
            }
            Text(text = uiState.savedLocation ?: "PDF not saved yet", style = MaterialTheme.typography.labelMedium)
        }

        ShareRow(onShare = onShare, onSharePdf = onSharePdf, pdfReady = uiState.savedUri != null)
    }
}

@Composable
fun StatusPanel(uiState: ReportUiState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Live status", style = MaterialTheme.typography.titleMedium)
                if (uiState.isRunning) {
                    CircularProgressIndicator(modifier = Modifier.padding(start = 8.dp))
                }
            }
            LazyColumn(modifier = Modifier.height(220.dp)) {
                items(uiState.statusMessages) { message ->
                    Text(text = "• $message", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareRow(
    onShare: (ShareTarget) -> Unit,
    onSharePdf: (ShareTarget) -> Unit,
    pdfReady: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "Share", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ShareIcon(label = "Facebook") { onShare(ShareTarget.FACEBOOK) }
            ShareIcon(label = "X") { onShare(ShareTarget.X) }
            ShareIcon(label = "LinkedIn") { onShare(ShareTarget.LINKEDIN) }
            ShareIcon(label = "Reddit") { onShare(ShareTarget.REDDIT) }
            if (pdfReady) {
                ShareIcon(label = "PDF") { onSharePdf(ShareTarget.GENERIC) }
            }
        }
    }
}

@Composable
fun ShareIcon(label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = onClick) {
            Icon(Icons.Default.Share, contentDescription = label)
        }
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun ReportDialog(report: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        icon = {
            Icon(Icons.Default.ThumbUp, contentDescription = null)
        },
        title = { Text("Report preview") },
        text = {
            LazyColumn(modifier = Modifier.height(360.dp)) {
                items(report.split("\n")) { line ->
                    Text(text = line, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    )
}
