# Implementation Plan - Remove Gemini Chat / AI Chat Section

This plan covers the complete removal of the Gemini Chat feature from the AITrader application to simplify the UI and reduce API quota usage.

## User Review Required

> [!IMPORTANT]
> This will remove the "Gemini ilə Söhbət" (Chat with Gemini) section from the Stock Detail screen. Existing chat logs in the history will still be visible but will no longer be generated.

## Proposed Changes

### [Component Name] UI Clean-up

#### [MODIFY] [StockDetailScreen.kt](file:///C:/Users/Cavansir/AndroidStudioProjects/AITrader/app/src/main/java/com/example/aitrader/StockDetailScreen.kt)
- Remove `chatHistory` state.
- Remove `isGeminiLoading` state.
- Remove `userQuery` state.
- Remove the chat UI components (Chat header, Chat message list, and the input row at the bottom).
- Wrap the main content in a `VerticalScroll` to ensure usability on smaller screens if the analysis card is large, or just remove the `weight(1f)` constraint that was previously on the chat list.

#### [MODIFY] [LogHistoryScreen.kt](file:///C:/Users/Cavansir/AndroidStudioProjects/AITrader/app/src/main/java/com/example/aitrader/LogHistoryScreen.kt)
- Remove special styling for "CHAT" decisions.
- Remove the conditional check `if (log.aiDecision != "CHAT")` to show timeframe stats for all logs (or simply remove the check if it was only to hide stats for chat logs).

### [Component Name] Logic Clean-up

#### [MODIFY] [TraderViewModel.kt](file:///C:/Users/Cavansir/AndroidStudioProjects/AITrader/app/src/main/java/com/example/aitrader/TraderViewModel.kt)
- Remove `askGeminiAboutStock` function.
- (Optional) Keep `GenerativeModel` as it is still used for `generateAISelfEvaluation` in the Performance screen.

## Verification Plan

### Automated Tests
- Run `./gradlew app:assembleDebug` to ensure the project still compiles.

### Manual Verification
1. Open the application.
2. Navigate to a stock's detail page.
3. Verify that the "Gemini ilə Söhbət" section is gone.
4. Verify that the rest of the stock detail information (Price, AI Analysis Recommendation, RSI, etc.) is still visible and correctly laid out.
5. Navigate to AI Logs and verify that the app doesn't crash if it encounters old "CHAT" entries.
