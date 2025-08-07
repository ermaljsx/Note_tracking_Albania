# Voice Notes AI (Android)

A minimal Android app that converts voice notes (Albanian/English) into structured, actionable notes using GPT-4. It transcribes speech with Android's SpeechRecognizer, enriches content via OpenAI, stores notes locally with Room, extracts tasks, and integrates with Calendar and reminders.

## Features
- Accurate speech-to-text for English (`en-US`) and Albanian (`sq-AL`) via Android SpeechRecognizer (optional Auto)
- GPT-4 enrichment (uses `gpt-4o-mini` by default) with JSON output: structured note, extracted tasks, and suggestions
- Local storage with Room: keeps original transcript and AI-enhanced note
- Task list with checkboxes, due dates (if parsed), one-tap Calendar insert and local reminders
- Minimal Compose UI: Record, Notes, Tasks, Settings (API key)

## Prerequisites
- Android Studio Hedgehog or newer
- Android SDK 34, JDK 17
- OpenAI API key (create at `https://platform.openai.com/`)

## Setup
1. Clone or copy this project.
2. Open in Android Studio. Let it sync Gradle.
3. Run the app on a device or emulator with microphone enabled.
4. In the app, open Settings → paste your OpenAI API key.

Permissions requested at runtime:
- Microphone (RECORD_AUDIO) for transcription
- Notifications (POST_NOTIFICATIONS) for reminders (Android 13+)

## How it works
- Tap Record to start listening; Stop to end. Transcript appears immediately.
- Tap Save → the app calls OpenAI to enrich the note, extracts tasks, then saves note+tasks locally.
- Tasks screen: check off tasks, add to calendar, or schedule a reminder.

## Models and prompting
- Default model: `gpt-4o-mini`. You can change it in `OpenAIRepository`.
- Prompt requests strictly JSON with fields: `ai_note_text`, `tasks`, `suggestions`.
- If JSON parsing fails, the raw model text is used as the AI note.

## Calendar & Reminders
- Calendar: opens an insert intent; user confirms in their calendar app.
- Reminders: schedules a local notification using AlarmManager. If no due date, a reminder is scheduled for 1 minute from now when tapping Remind.

## Local storage
- Database: `voice_notes_ai.db` with tables `notes` and `tasks`.

## Testing scenarios
- Albanian: “Kujto të blesh qumësht dhe vezë nesër.” → Ensure transcript in Albanian; AI outputs checklist and extracts a task with due date.
- Idea in English: “I’m thinking of starting a small garden…” → AI returns a step-by-step plan and tasks.
- Shopping list: “carrots, chicken, rice” → AI formats list and suggests healthy additions.
- Reminder: “Meeting on July 20th at 3 PM with team” → Use Calendar button; test Remind.

## Notes
- All notes/tasks are stored locally on-device. No cloud sync (yet).
- SpeechRecognizer accuracy depends on network and device; Albanian requires network.
- For higher accuracy, you could integrate Whisper API by uploading recorded audio, but this app sticks to the built-in recognizer for simplicity.

## Build
- In Android Studio: Run ▶
- Or via CLI (requires local Gradle and Android SDK):
  `./gradlew :app:assembleDebug`

## Future improvements
- Whisper integration as an option; offline transcription where available
- Better task/date parsing via model function calling or JSON schema
- Deep links from notifications to tasks
- Cloud backup, iOS version, AI chat over notes