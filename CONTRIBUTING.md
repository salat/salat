# How to Contribute

See also: [Contributing to Open Source on GitHub](https://guides.github.com/activities/contributing-to-open-source/)

1. Fork the salat project on github (https://github.com/salat/salat)
2. Identify the correct target branch (see "Which Version/Branch?" below)
3. Create a branch in your fork.
4. Make your changes. Be sure that unit tests still pass! (Run `sbt test:test`)
5. Open a pull request for review. 

# Getting Started 

Getting up to develop Salat requires three high-level steps

1. Install SBT (recommended: sbt: the rebel cut: https://github.com/paulp/sbt-extras)
2. Install the appropriate MongoDB version (this is required for unit tests): https://docs.mongodb.com/manual/installation/
3. `sbt test:test` to ensure the project compiles and all unit tests pass.

# Which Version/Branch?

Salat currently has three branches:

| Branch          | Latest Version | Description |
|-----------------|----------------|-------------|
| `1.9.x-branch`  | `1.9.10`       | Legacy support for the 1.9.x releases - conservative bug fixes and enhancements. |
| `1.10.x-branch` | `1.10.0`       | Home of `1.10.x` versions, and all community-submitted fixes / enhancements. Legacy support for older Scala and Casbah releases. Scala 2.10 / 2.11 and Casbah 2.8.2 support. |
| `1.11.x-branch` | None           | Home of `1.11.x` versions, and all community-submitted fixes / enhancements. Scala 2.11 / 2.12 and Casbah 3.1.x support. |
| `master`        | None           | Home of `2.0.0-SNAPSHOT` - mysterious future development. |

In general, you shouldn't submit PRs against `master` (well, you can, but we'll likely re-direct them to the `1.10.x-branch` - see below). `master`, which as of July 2016 is on version `2.x.x`, is reserved for any future development by Salat project lead @rktoomey as she sees fit, on her development schedule.

`1.10.x-branch` and `1.11.x-branch` are the branches for of ongoing, active development of Salat, via community-submitted changes.  Resulting bugs are likewise detected and fixed by the community. Use at your own discretion.

Finally, the branch `1.9.x-branch` is a "legacy-support" branch for simple bug fixes and enhancements on the 1.9.x codebase. No major, earth-shattering code changes occur on this branch. Major enhancements on the 1.10.x and 2.x.x branches will generally not be backported to this branch.

# Copyright

Each source file should have the project copyright text as a header. Template text (as a Velocity Templates file) is found in `notes/copyright.vm`. The most conventient method of using this template file (aside from the requisite yak-shaving) is the IntelliJ IDEA "Copyright Manager" plugin.

See: https://www.jetbrains.com/help/idea/2016.1/generating-and-updating-copyright-notice.html

If adding new files to the project, please either copy/paste/tweak the copyright header comments into the file, or (if using IntellliJ) use the Copyright Manager plugin.

# Code Formatting and Style

Salat uses [Scalariform](http://scala-ide.org/scalariform/) to enforce consistent formatting of source code. So, after compilation, you might discover that Scalariform has changed whitespace formatting of your source code. As such, please at least run `sbt test:compile` at least once before commiting your changes and submitting a PR.

You can find the Scalariform configuration in the project build file, `project/SalatBuild.scala`.
