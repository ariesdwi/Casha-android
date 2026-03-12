package com.casha.app.ui.feature.onboarding

import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.casha.app.R
import com.casha.app.ui.feature.onboarding.components.PageIndicator
import com.casha.app.ui.feature.onboarding.model.OnboardingPage
import com.casha.app.ui.feature.onboarding.pages.*

@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val currentPage by viewModel.currentPage.collectAsState()
    val isCompleted by viewModel.isCompleted.collectAsState()

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { OnboardingPage.entries.size }
    )

    // Navigate to next screen when onboarding completes
    LaunchedEffect(isCompleted) {
        if (isCompleted) onOnboardingComplete()
    }

    // Sync pager with ViewModel
    LaunchedEffect(currentPage) {
        if (pagerState.currentPage != currentPage.index) {
            pagerState.animateScrollToPage(
                currentPage.index,
                animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f)
            )
        }
    }

    // Sync ViewModel with pager (user swipe)
    LaunchedEffect(pagerState.currentPage) {
        val page = OnboardingPage.fromIndex(pagerState.currentPage)
        if (page != currentPage) {
            viewModel.setPage(page)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header: Skip button
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxWidth()
                    .height(56.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!currentPage.isLastPage) {
                    TextButton(
                        onClick = { viewModel.skip() },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.onboarding_skip),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Paging Content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (OnboardingPage.fromIndex(page)) {
                    OnboardingPage.HOOK -> HookPage()
                    OnboardingPage.CHAT_INPUT -> ChatInputPage()
                    OnboardingPage.REVELATION -> RevelationPage()
                    OnboardingPage.TRANSACTION_HISTORY -> TransactionHistoryPage()
                    OnboardingPage.BUDGET_TRACKER -> BudgetTrackerPage()
                    OnboardingPage.PORTFOLIO -> PortfolioPage()
                    OnboardingPage.LIABILITIES -> LiabilitiesPage()
                    OnboardingPage.GOAL_TRACKER -> GoalTrackerPage()
                    OnboardingPage.GLOBAL_POWER -> GlobalPowerPage()
                    OnboardingPage.OFFLINE_SYNC -> OfflineSyncPage(onStart = { viewModel.skip() })
                }
            }

            // Footer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                PageIndicator(
                    numberOfPages = OnboardingPage.entries.size,
                    currentPage = pagerState.currentPage
                )

                if (!currentPage.isLastPage) {
                    Button(
                        onClick = { viewModel.nextPage() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .padding(bottom = 24.dp)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = stringResource(currentPage.ctaTextResId),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
