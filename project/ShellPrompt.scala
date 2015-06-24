import sbt._

// Shell prompt which show the current project, git branch and build version
object ShellPrompt {

  object devnull extends ProcessLogger {
    def info (s: => String) {}
    def error (s: => String) { }
    def buffer[T] (f: => T): T = f
  }

  def currBranch() = ("git rev-parse --abbrev-ref HEAD" lines_! devnull).headOption.getOrElse("???")

  private def parseToBool(cmd: String) = (cmd lines_! devnull).headOption.exists(_ == "true")

  private def successfulExit(cmd: String) = cmd ! devnull == 0

  private def unsuccessfulExit(cmd: String) = !successfulExit(cmd)

  def showDirtyState() = {
    // transposed from git-completion.bash
    val insideGitDir = parseToBool("git rev-parse --is-inside-git-dir")
    val insideWorkTree = !insideGitDir && parseToBool("git rev-parse --is-inside-work-tree")
    val showDirtyState = insideWorkTree && unsuccessfulExit("git config --bool bash.showDirtyState") // for some reason, this exits with 1 but that's what we want
    if (showDirtyState) {
      // modified
      val w = if (unsuccessfulExit("git diff --no-ext-diff --quiet --exit-code")) "*" else ""
      // added
      val i = if (successfulExit("git rev-parse --quiet --verify HEAD") && unsuccessfulExit("git diff-index --cached --quiet HEAD --")) "+" else ""
      // stashed
      val s = if (successfulExit("git rev-parse --verify refs/stash")) "$" else ""
      // untracked
      val u = if (("git ls-files --others --exclude-standard" lines_! devnull).nonEmpty) "%" else ""
      "%s%s%s%s".format(w, i, s, u)
    }
    else ""
  }

  private def showUpstream() = {
    val counts = ("git rev-list --count --left-right @{upstream}...HEAD" lines_! devnull).headOption.getOrElse("")
    if (counts.isEmpty) "" // no upstream
    else if (counts == "0\t0") "=" // equal to upstream
    else if (counts.startsWith("0")) ">" // ahead of upstream
    else if (counts.endsWith("\t0")) "<" // behind upstream
    else "<>" // oh crumbs
  }

  val buildShellPrompt = (state: State) => {
    // thanks to https://groups.google.com/forum/#!searchin/simple-build-tool/How$20to$20color$20ShellPrompt/simple-build-tool/H7HcaYiv8FM/lMa2aVgCa3gJ
    // and http://mediadoneright.com/content/ultimate-git-ps1-bash-prompt
    val p = Project.extract(state)
    val projectNameOrId = p.getOpt(sbt.Keys.name).getOrElse(p.currentProject.id)
    val projectVersion = p.getOpt(sbt.Keys.version).getOrElse("")

    import scala.Console.RESET
    val YELLOW_I = "\033[0;92m"
    val GREEN_I = "\033[0;92m"
    val RED_I = "\033[0;91m"
    val BLACK_I = "\033[0;90m"
    val CYAN_I = "\033[0;96m"

    val projectVersionColor =
      if (("git status" lines_! devnull).filter(_.contains("nothing to commit")).nonEmpty) GREEN_I
      else RED_I

    s"[${CYAN_I}$projectNameOrId$RESET] $projectVersionColor{${currBranch()} ${showDirtyState()}${showUpstream()}} $RESET$$ "
  }
}