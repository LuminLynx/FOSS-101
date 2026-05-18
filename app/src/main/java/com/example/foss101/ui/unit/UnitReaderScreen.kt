package com.example.foss101.ui.unit

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foss101.data.repository.CompletionCache
import com.example.foss101.data.repository.PathRepository
import com.example.foss101.model.CalibrationTag
import com.example.foss101.model.Grade
import com.example.foss101.model.RubricCriterion
import com.example.foss101.model.UnitSource
import com.example.foss101.ui.components.AppScreenScaffold
import com.example.foss101.ui.components.CalibrationTier
import com.example.foss101.ui.components.MarkdownText
import com.example.foss101.ui.components.PrimaryActionButton
import com.example.foss101.ui.components.SectionHeader
import com.example.foss101.ui.components.TagChip
import com.example.foss101.ui.components.screenContentPadding
import com.example.foss101.ui.theme.JetBrainsMono
import com.example.foss101.ui.theme.LibellaTheme
import com.example.foss101.viewmodel.UnitReaderEvent
import com.example.foss101.viewmodel.UnitReaderUiState
import com.example.foss101.viewmodel.UnitReaderViewModel

@Composable
fun UnitReaderScreen(
    pathRepository: PathRepository,
    completionCache: CompletionCache,
    unitId: String,
    onAuthExpired: () -> Unit
) {
    val viewModel: UnitReaderViewModel = viewModel(
        key = unitId,
        factory = UnitReaderViewModel.factory(pathRepository, completionCache, unitId)
    )

    val state = viewModel.uiState

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                UnitReaderEvent.AuthExpired -> onAuthExpired()
            }
        }
    }

    val title = when (state) {
        is UnitReaderUiState.Loaded -> state.unit.title
        else -> "Unit"
    }
    val subtitle = when (state) {
        is UnitReaderUiState.Loaded -> "Unit ${state.unit.position}"
        else -> ""
    }

    AppScreenScaffold(title = title, subtitle = subtitle) { contentPadding ->
        when (state) {
            is UnitReaderUiState.Loading -> LoadingBox(modifier = Modifier.screenContentPadding(contentPadding))
            is UnitReaderUiState.Error -> ErrorBox(
                message = if (state.authExpired) "Sign in to continue." else state.message,
                onRetry = viewModel::load,
                modifier = Modifier.screenContentPadding(contentPadding)
            )
            is UnitReaderUiState.Loaded -> LoadedBody(
                state = state,
                onToggleTradeOff = viewModel::toggleTradeOff,
                onToggleDepth = viewModel::toggleDepth,
                onAnswerChanged = viewModel::onAnswerChanged,
                onSubmitAnswer = viewModel::submitAnswer,
                modifier = Modifier.screenContentPadding(contentPadding)
            )
        }
    }
}

@Composable
private fun LoadedBody(
    state: UnitReaderUiState.Loaded,
    onToggleTradeOff: () -> Unit,
    onToggleDepth: () -> Unit,
    onAnswerChanged: (String) -> Unit,
    onSubmitAnswer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val unit = state.unit
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = unit.definition,
            style = MaterialTheme.typography.bodyLarge
        )

        // Order follows the Loop in STRATEGY.md (line 108–115):
        //   2. Bite — read the concept.
        //   3. Decide — answer the prompt before seeing the consensus.
        //   4. Calibrate — settled / contested + sources, after the answer.
        // Trade-off framing and Depth are tap-to-expand reference material
        // (P4 "bite first, depth on tap"). Showing calibration / sources
        // before the decision prompt would prime the answer, which is
        // what P2 ("calibrate, don't bluff") explicitly avoids.

        Section(title = "90-second bite") {
            MarkdownText(markdown = unit.biteMd)
        }

        MarkdownDisclosure(
            label = "Trade-off framing",
            markdown = unit.tradeOffFraming,
            expanded = state.tradeOffExpanded,
            onToggle = onToggleTradeOff
        )

        MarkdownDisclosure(
            label = "Depth",
            markdown = unit.depthMd,
            expanded = state.depthExpanded,
            onToggle = onToggleDepth
        )

        unit.decisionPrompt?.let { prompt ->
            Section(title = "Decision prompt") {
                MarkdownText(markdown = prompt.promptMd)
            }

            // F3 — open-ended answer input. F4 — submit + render grade output.
            // Decide before Calibrate (Loop step 3 → 4) so the consensus
            // signal doesn't prime the answer.
            DecisionAnswerSection(
                state = state,
                onAnswerChanged = onAnswerChanged,
                onSubmit = onSubmitAnswer
            )

            state.gradeResult?.let { result ->
                GradeOutputSection(
                    grades = result.grades,
                    flagged = result.flagged,
                    rubricCriteriaById = unit.rubric?.criteria.orEmpty().associateBy { it.id }
                )
            }
        }

        if (unit.calibrationTags.isNotEmpty()) {
            Section(title = "Calibration") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    unit.calibrationTags.forEach { tag -> CalibrationTagRow(tag) }
                }
            }
        }

        if (unit.sources.isNotEmpty()) {
            Section(title = "Sources") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    unit.sources.forEach { source -> SourceRow(source) }
                }
            }
        }

        if (state.isCompleted && state.gradeResult == null) {
            // Completed in a prior session — the grade output isn't loaded
            // (we don't fetch grades on unit open today), so just confirm.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                StatusGlyph(met = true)
                Text(
                    text = "You completed this unit in a previous session.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun Section(title: String, body: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        SectionHeader(title = title)
        body()
    }
}

// la-check: 18dp ring; met = filled oxblood with paper check,
// not-met = error ring with an error cross.
@Composable
private fun StatusGlyph(met: Boolean) {
    val oxblood = MaterialTheme.colorScheme.primary
    val error = MaterialTheme.colorScheme.error
    Box(
        modifier = Modifier
            .size(18.dp)
            .clip(CircleShape)
            .background(if (met) oxblood else Color.Transparent)
            .border(1.5.dp, if (met) oxblood else error, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (met) Icons.Filled.Check else Icons.Filled.Close,
            contentDescription = if (met) "Met" else "Not met",
            tint = if (met) MaterialTheme.colorScheme.onPrimary else error,
            modifier = Modifier.size(12.dp)
        )
    }
}

@Composable
private fun DecisionAnswerSection(
    state: UnitReaderUiState.Loaded,
    onAnswerChanged: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = state.answerDraft,
            onValueChange = onAnswerChanged,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp),
            label = { Text("YOUR ANSWER", style = MaterialTheme.typography.titleSmall) },
            placeholder = {
                Text(
                    "Be specific about what you'd measure, what you'd ignore, " +
                        "and where your estimate might still be wrong.",
                    color = LibellaTheme.colors.inkTertiary
                )
            },
            enabled = !state.submitInProgress,
            minLines = 4,
            maxLines = 12,
            shape = RoundedCornerShape(2.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = LibellaTheme.colors.hairline,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedLabelColor = LibellaTheme.colors.inkTertiary,
                unfocusedLabelColor = LibellaTheme.colors.inkTertiary,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )

        val canSubmit = state.answerDraft.trim().isNotEmpty() && !state.submitInProgress
        val ctaLabel = when {
            state.submitInProgress -> "Grading…"
            state.gradeResult != null -> "Re-submit"
            else -> "Submit answer"
        }
        PrimaryActionButton(
            text = ctaLabel,
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth(),
            enabled = canSubmit
        )

        state.submitFailure?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun GradeOutputSection(
    grades: List<Grade>,
    flagged: Boolean,
    rubricCriteriaById: Map<Long, RubricCriterion>
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (flagged) {
            // STRATEGY.md T2-B: "If the grader is uncertain anywhere, the
            // answer is flagged 'review needed' and the canonical answer
            // is shown instead of a pass/fail." We deliberately do NOT
            // render the per-criterion GradeRow cards in this branch:
            // showing Met/Not-Met chips next to a "we're not confident"
            // banner is contradictory and undermines the flagged-or-graded
            // contract.
            //
            // v1 doesn't yet store a separate authored canonical answer,
            // so the banner points the user at the calibration tags +
            // sources below as the surrogate. Adding canonical-answer
            // content is tracked as Phase-2 polish.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(2.dp))
                    .background(LibellaTheme.colors.bannerTint)
                    .border(1.dp, LibellaTheme.colors.hairline, RoundedCornerShape(2.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Review needed".uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    color = LibellaTheme.colors.onBannerTint
                )
                Text(
                    text = "We're not confident enough to grade this fairly. " +
                        "Compare your answer to the calibration tags and sources below, then try again.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            return@Column
        }

        SectionHeader(title = "Your grade")
        // Sort grades by the criterion's authored position so the UI
        // mirrors the rubric order, not the order grades came back in.
        val ordered = grades.sortedBy { rubricCriteriaById[it.criterionId]?.position ?: Int.MAX_VALUE }
        ordered.forEach { grade ->
            val criterion = rubricCriteriaById[grade.criterionId]
            GradeRow(grade = grade, criterionText = criterion?.text ?: "Criterion ${grade.criterionId}")
        }
    }
}

@Composable
private fun GradeRow(
    grade: Grade,
    criterionText: String
) {
    val pct = (grade.confidence * 100).toInt()
    val highConfidence = pct >= 80
    val badgeBg = if (highConfidence) LibellaTheme.colors.unsettledTint else LibellaTheme.colors.contestedTint
    val badgeInk = if (highConfidence) LibellaTheme.colors.onUnsettledTint else LibellaTheme.colors.onContestedTint
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(2.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, LibellaTheme.colors.hairline, RoundedCornerShape(2.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatusGlyph(met = grade.met)
            Text(
                text = criterionText,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 15.sp),
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "$pct%",
                color = badgeInk,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = JetBrainsMono,
                    fontSize = 11.sp
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(2.dp))
                    .background(badgeBg)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
        Text(
            text = grade.rationale,
            style = MaterialTheme.typography.bodyMedium
        )
        if (grade.answerQuote.isNotBlank()) {
            Text(
                text = "“${grade.answerQuote}”",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .drawLeftRule(LibellaTheme.colors.hairline)
                    .padding(start = 10.dp)
            )
        }
    }
}

private fun Modifier.drawLeftRule(color: Color): Modifier = this.drawBehind {
    drawLine(
        color = color,
        start = Offset(0f, 0f),
        end = Offset(0f, size.height),
        strokeWidth = 1.dp.toPx()
    )
}

@Composable
private fun MarkdownDisclosure(
    label: String,
    markdown: String,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    if (markdown.isBlank()) return
    Column {
        HorizontalDivider(thickness = 1.dp, color = LibellaTheme.colors.hairline)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.weight(1f)
            )
            Text(
                text = if (expanded) "OPEN" else "TAP TO EXPAND",
                style = MaterialTheme.typography.titleSmall,
                color = LibellaTheme.colors.inkTertiary
            )
            Icon(
                imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = if (expanded) "Hide $label" else "Show $label",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        AnimatedVisibility(visible = expanded) {
            MarkdownText(
                markdown = markdown,
                modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
            )
        }
    }
}

@Composable
private fun CalibrationTagRow(tag: CalibrationTag) {
    val tier = when (tag.tier.lowercase()) {
        "settled" -> CalibrationTier.Settled
        "contested" -> CalibrationTier.Contested
        else -> CalibrationTier.Unsettled
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TagChip(label = tag.tier, tier = tier)
        Text(
            text = tag.claim,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SourceRow(source: UnitSource) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(2.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, LibellaTheme.colors.hairline, RoundedCornerShape(2.dp))
            .clickable {
                runCatching {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(source.url))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }
            }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = source.title,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 15.sp)
            )
            Text(
                text = "${if (source.primarySource) "Primary · " else ""}${source.date}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
            contentDescription = "Open in browser",
            tint = LibellaTheme.colors.inkTertiary
        )
    }
}

@Composable
private fun LoadingBox(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun ErrorBox(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        PrimaryActionButton(
            text = "Retry",
            onClick = onRetry,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}
