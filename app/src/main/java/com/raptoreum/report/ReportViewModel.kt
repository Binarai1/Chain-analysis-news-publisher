package com.raptoreum.report

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


data class ReportUiState(
    val isRunning: Boolean = false,
    val statusMessages: List<String> = emptyList(),
    val report: String? = null,
    val savedLocation: String? = null,
    val savedUri: Uri? = null,
    val errorMessage: String? = null
)

class ReportViewModel(
    private val scraperRepository: ScraperRepository = ScraperRepository(),
    private val reportBuilder: ReportBuilder = ReportBuilder(),
    private val pdfExporter: PdfExporter = PdfExporter(),
    private val shareHelper: ShareHelper = ShareHelper()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState

    fun startScrape(customTargets: List<ScrapeTarget> = emptyList()) {
        _uiState.value = ReportUiState(isRunning = true, statusMessages = listOf("Starting scrape…"))
        viewModelScope.launch {
            val status = mutableListOf("Starting scrape…")
            val payload = scraperRepository.scrapeAll(
                updateStatus = { message ->
                    status.add(message)
                    _uiState.value = _uiState.value.copy(statusMessages = status.toList())
                },
                customTargets = customTargets
            )
            val report = reportBuilder.buildReport(payload)
            status.add("Report assembled")
            _uiState.value = ReportUiState(
                isRunning = false,
                statusMessages = status,
                report = report
            )
        }
    }

    fun saveReport(context: Context) {
        val reportText = _uiState.value.report ?: return
        viewModelScope.launch {
            runCatching {
                val location = pdfExporter.export(context, reportText)
                _uiState.value = _uiState.value.copy(
                    savedLocation = location,
                    savedUri = Uri.parse(location)
                )
            }.onFailure { throwable ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = throwable.localizedMessage
                )
            }
        }
    }

    fun shareText(context: Context, target: ShareTarget) {
        val reportText = _uiState.value.report ?: return
        shareHelper.shareText(context, reportText, target)
    }

    fun sharePdf(context: Context, target: ShareTarget) {
        val uri = _uiState.value.savedUri ?: return
        shareHelper.sharePdf(context, uri, target)
    }
}
