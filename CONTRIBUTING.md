# Contributing to OpenGridSeg

Thank you for helping improve OpenGridSeg.

## Report a problem

Use the GitHub bug-report form. Include:

- what you expected;
- what happened;
- Fiji and operating-system versions;
- a small example filename pattern;
- steps that reproduce the problem;
- an error message or screenshot, when available.

Do not upload private microscopy data without permission.

## Suggest a feature

Use the GitHub feature-request form. Explain the user problem first, then the proposed change.

## Make a code change

1. Fork the repository and create a focused branch.
2. Use JDK 21 and Maven.
3. Add or update tests with the change.
4. Run:

   ```bash
   mvn clean test package
   ```

5. Open a pull request that explains the change and how it was tested.

## Project rules

- Keep exported pixels in source digital-number units.
- Never make display scaling affect scoring or export.
- Keep the plugin usable without Python.
- Keep Java 8-compatible bytecode for Fiji.
- Do not add private datasets, generated exports, or credentials.
- Keep changes small and easy to review.

By contributing, you agree that your contribution is released under the MIT License.
