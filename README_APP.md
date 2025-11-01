# MDFC Android App

## Overview
This is an Android application built with Kotlin that provides quick access to two WBMDFC portals:
- **TERM LOAN**: https://tl.wbmdfc.org/district/login
- **DLS**: https://dls.wbmdfc.org/login-district

## Features
- Clean, modern Material Design UI
- Two prominent buttons on the home screen for easy navigation
- WebView implementation that loads websites in **desktop mode**
- Progress bar showing page load status
- Back button support for navigation within WebView
- Action bar with back navigation

## Technical Details

### Components Created/Modified

1. **MainActivity.kt**
   - Home screen with two Material buttons
   - Click handlers to launch WebViewActivity with respective URLs

2. **WebViewActivity.kt**
   - Displays websites in desktop mode using custom user agent
   - JavaScript enabled for full functionality
   - DOM storage and database enabled
   - Zoom controls enabled (without display controls)
   - Progress bar for loading indication
   - Back button navigation support

3. **Layouts**
   - `activity_main.xml`: Clean centered layout with two elevated Material buttons
   - `activity_webview.xml`: WebView with progress bar

4. **Resources**
   - Updated `strings.xml` with app title and button labels
   - Updated `colors.xml` with Material Design color palette
   - Added internet permissions in `AndroidManifest.xml`

### Key Features of WebView Implementation
- **Desktop Mode**: Uses desktop user agent string to load full desktop versions of websites
- **JavaScript Enabled**: Full support for interactive web applications
- **Mixed Content**: Allows loading of mixed HTTP/HTTPS content
- **Caching**: Enabled for better performance
- **Navigation**: Hardware back button support for going back in browsing history

## Building the App

### Prerequisites
- Android Studio Arctic Fox or later
- JDK 11 or later
- Android SDK with API level 28 (minimum) to 36 (target)

### Build Instructions

1. Open the project in Android Studio
2. Sync Gradle files
3. Build the project:
   ```bash
   ./gradlew build
   ```
4. Run on emulator or device:
   ```bash
   ./gradlew installDebug
   ```

### Running on Device
1. Enable Developer Options on your Android device
2. Enable USB Debugging
3. Connect device via USB
4. Click "Run" in Android Studio or use:
   ```bash
   ./gradlew installDebug
   ```

## App Structure
```
app/
├── src/main/
│   ├── java/com/sam/mdfc/
│   │   ├── MainActivity.kt          # Home screen with buttons
│   │   └── WebViewActivity.kt       # WebView for displaying websites
│   ├── res/
│   │   ├── layout/
│   │   │   ├── activity_main.xml    # Home screen layout
│   │   │   └── activity_webview.xml # WebView layout
│   │   ├── values/
│   │   │   ├── strings.xml          # String resources
│   │   │   └── colors.xml           # Color palette
│   └── AndroidManifest.xml          # App configuration
```

## Design Choices

### UI/UX
- **Material Design 3**: Using Material buttons with elevation for modern look
- **Clean Layout**: Centered vertical layout with adequate spacing
- **Color Scheme**: Professional blue primary color (#1976D2)
- **Typography**: Bold, large text for easy readability
- **Icons**: Compass icons to indicate navigation/portal access

### Technical
- **Desktop Mode**: Ensures users see full-featured desktop versions of the websites
- **Edge-to-Edge**: Modern Android UI with edge-to-edge display
- **Configuration Changes**: Handles orientation changes without reloading
- **Progress Indication**: Visual feedback during page loading

## Permissions Required
- `INTERNET`: Required for loading web content
- `ACCESS_NETWORK_STATE`: For checking network connectivity

## Minimum Requirements
- Android 9.0 (API 28) or higher
- Internet connection

## Notes
- The app loads websites in desktop mode by default
- Both websites require internet connectivity
- WebView maintains browsing history for back navigation
- The app uses Material Design 3 components for a modern look
