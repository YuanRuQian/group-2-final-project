#!C:/Program\ Files/Git/usr/bin/sh.exe


# Run Gradle tasks to add ktlint pre-commit hooks
./gradlew addKtlintFormatGitPreCommitHook

# Display the content of the pre-commit hook
cat .git/hooks/pre-commit
