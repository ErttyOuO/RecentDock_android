# Upload to GitHub

Repository name: **RecentDock_android**

## 1. Create the repository

1. On GitHub, create a new repository named `RecentDock_android`.
2. Choose Public or Private according to your publishing plan.
3. Create it as an empty repository. Do not ask GitHub to generate another README, `.gitignore`, or LICENSE because this folder already contains its own README and `.gitignore`.
4. Before choosing Public, confirm the rights to the source code and image assets. This prepared source folder intentionally does not add a LICENSE.

## 2. Upload with Git

Open PowerShell and run the following commands. Replace `<YOUR_GITHUB_USERNAME>` with your GitHub account name.

    cd "C:\path\to\RecentDock-\github"
    git init
    git add .
    git update-index --chmod=+x gradlew
    git commit -m "Initial source: RecentDock v0.8.3"
    git branch -M main
    git remote add origin https://github.com/<YOUR_GITHUB_USERNAME>/RecentDock_android.git
    git push -u origin main

## 3. Upload through the GitHub website

If you prefer the website, open the empty repository, choose **Add file → Upload files**, and upload the contents inside the local `github` folder. Upload the files and folders inside it directly so `README.md`, `app/`, and the Gradle files appear at the repository root. Do not upload the enclosing `github` folder as an extra nested directory.

## Included / excluded

- Included: Android app source, resources, unit tests, Gradle build configuration, bootstrap scripts, README, release notes, validation report and release-readiness notes.
- Excluded: `local.properties`, IDE settings, Gradle caches, temporary sync data, ZIP backups, APK/AAB outputs and signing files.
- No Git repository has been initialized in this folder yet; the commands above initialize it when you are ready.
- No APK or formal GitHub Release is included. The current code is a v0.8.3 source candidate pending a full Gradle build and device testing.
