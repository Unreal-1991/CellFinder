5G CELL ID FINDER — ANDROID SOURCE PROJECT

This project builds a debug APK using GitHub Actions. It attempts to read cell info from the phone where the app is installed. Android/carrier firmware may hide 5G NR NCI/TAC, especially for 5G NSA.

PHONE-ONLY BUILD STEPS
1. Extract this ZIP on your phone or upload its contents to a new GitHub repository.
2. In GitHub, create a repository and upload the files/folders preserving the structure. Easier: use GitHub website's Add file > Upload files after extracting the ZIP.
3. Commit files to the main branch.
4. Open Actions tab > Build Cell ID Finder APK > Run workflow.
5. When it finishes successfully, open the workflow run and download the artifact named Cell-ID-Finder-APK.
6. Extract the downloaded artifact ZIP to find app-debug.apk, then install it. Android may ask permission to install unknown apps for your browser/file manager.
7. Open app and allow Phone + Location permissions. Turn Location on, keep SIM enabled, press BASAHIN ULIT.

If Android Studio/Gradle build fails, open the red failed step and share the full error log. Do not share passwords, tokens, or private repository secrets.
