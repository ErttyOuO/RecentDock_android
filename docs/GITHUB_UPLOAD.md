# Upload to GitHub

Repository name: **RecentDock_android**

## 1. Create the repository

1. On GitHub, create a new repository named `RecentDock_android`.
2. Choose Public or Private according to your publishing plan.
3. Create it as an empty repository. Do not ask GitHub to generate another README, `.gitignore`, or LICENSE because this folder already contains its own README and `.gitignore`.
4. Before choosing Public, confirm the rights to the source code and image assets. This prepared source folder intentionally does not add a LICENSE.

## 2. Commit and push the prepared folder

The local `github` folder is already a Git repository on branch `main`, with `origin` configured to your GitHub repository. After changing files, open PowerShell and run:

    cd "C:\path\to\RecentDock-\github"
    git status
    git add .
    git commit -m "Fix uninstall permission: RecentDock v0.8.4"
    git push -u origin main

## 3. Upload through the GitHub website

If you prefer the website, open the empty repository, choose **Add file → Upload files**, and upload the contents inside the local `github` folder. Upload the files and folders inside it directly so `README.md`, `app/`, and the Gradle files appear at the repository root. Do not upload the enclosing `github` folder as an extra nested directory.

## Included / excluded

- Included: Android app source, resources, unit tests, Gradle build configuration, bootstrap scripts, README, release notes, validation report and release-readiness notes.
- Excluded: `local.properties`, IDE settings, Gradle caches, temporary sync data, ZIP backups, APK/AAB outputs and signing files.
- Git is already initialized locally; the `main` branch and `origin` remote are configured.
- No APK or formal GitHub Release is included. The current code is a v0.8.4 source candidate pending a full Gradle build and device testing.
