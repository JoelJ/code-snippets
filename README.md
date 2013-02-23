Used just like code-snippet sites like Pastebin.com or Pastie.org.

Files are added $JENKINS_HOME/codesnippets. If the directory doesn't exist, Jenkins will attempt to create it for you.

To access the snippets, click on the "Code Snippets" entry on the left bar.
A list of snippets will appear on the bottom if any exist.

Access is based on Project access. So if a person has permission to CREATE, READ, or DELETE a Project, they will have the same abilities with Snippets.

Todo:
-----
* Create a new Permissions set unique for snippets. That way a user can have permissions to upload a snippet but not create a project.
