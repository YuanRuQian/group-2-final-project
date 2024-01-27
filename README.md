# Group 2 - Trip Planning App Demo

## Members
- Kai He
- Jerry Xu
- Brain Zhang
- Lydia Yuan

## Pre Dev Setup

First, sync gradle files in the project. This will download all the dependencies needed for the project. ( `File > Sync Project with Gradle Files`)

### Pre-commit hook setup

Choose which script to run base on your OS. If you are using Windows, run [`pre_dev_setup_windows.sh`](pre_dev_setup_windows.sh). If you are using Linux or MacOS, run [`pre_dev_setup_unix.sh`](pre_dev_setup_unix.sh).

```bash
chmod +x ./script_name # Replace script_name with the script name
./script_name # Replace script_name with the script name
```

Please review the script output for any potential errors. This script is designed to establish pre-commit hooks for code formatting using ktlint.

### Firebase configuration setup

Setting up Firebase configuration in your project is an essential step to enable Firebase services. Here's a straightforward guide on how to do it:

1. **Go to Firebase Console Project Settings:**
   - Navigate to the Firebase Console (https://console.firebase.google.com/).
   - Select your project or create a new one if you haven't already.

2. **Download the config file `google-services.json`:**
   - Go to the [app page](https://console.firebase.google.com/project/group-2-final-project/settings/general/android:group.two.tripplanningapp).
   - Download the `google-services.json` file.

3. **Put the file in the `androidApp/app` directory of the project:**
   - In your project's file structure, place the downloaded `google-services.json` file in the `androidApp/app` directory. This is typically where your Android app's configuration files reside.

4. **You are all set!**
   - With the `google-services.json` file in place, your project is now configured to use Firebase services. You can start integrating Firebase features into your app.
   - If you encounter problems, try cleaning and rebuilding your project, or try invalidating caches and restarting Android Studio.
