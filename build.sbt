import com.typesafe.sbt.SbtGhPages.{ghpages, GhPagesKeys => ghkeys}
import com.typesafe.sbt.SbtGit.{git, GitKeys}
import com.typesafe.sbt.git.GitRunner

lazy val root = (project in file(".")).
  enablePlugins(PamfletPlugin).
  settings(
    organization := "org.foundweekends",
    name := "foundweekends_website",
    ghpages.settings,
    git.remoteRepo := "git@github.com:foundweekends/foundweekends.github.io.git",
    GitKeys.gitBranch in ghkeys.updatedRepository := Some("master"),
    // This task is responsible for updating the master branch on some temp dir.
    // On the branch there are files that was generated in some other ways such as:
    // - CNAME file
    //
    // This task's job is to call "git rm" on files and directories that this project owns
    // and then copy over the newly generated files.
    ghkeys.synchLocal := {
      // sync the generated site
      val repo = ghkeys.updatedRepository.value
      val s = streams.value
      val r = GitKeys.gitRunner.value
      gitRemoveFiles(repo, (repo * "*.html").get.toList, r, s)
      val mappings =  for {
        (file, target) <- siteMappings.value if siteInclude(file)
      } yield (file, repo / target)
      IO.copy(mappings)
      repo
    }
  )

def siteInclude(f: File) = true
def gitRemoveFiles(dir: File, files: List[File], git: GitRunner, s: TaskStreams): Unit = {
  if(!files.isEmpty)
    git(("rm" :: "-r" :: "-f" :: "--ignore-unmatch" :: files.map(_.getAbsolutePath)) :_*)(dir, s.log)
  ()
}
