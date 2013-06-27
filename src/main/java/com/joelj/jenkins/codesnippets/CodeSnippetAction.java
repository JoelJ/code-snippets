package com.joelj.jenkins.codesnippets;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.RootAction;
import hudson.model.User;
import hudson.security.ACL;
import hudson.security.Permission;
import jenkins.model.Jenkins;
import org.apache.commons.io.FileUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * User: Joel Johnson
 * Date: 2/23/13
 * Time: 11:45 AM
 */
@Extension
public class CodeSnippetAction implements RootAction {
	private static final Logger LOG = Logger.getLogger("code-snippets");
	private static final File DIRECTORY = new File(Jenkins.getInstance().getRootDir(), "codesnippets");
	public static final String UTF_8 = "UTF-8";

	public void doAdd(StaplerRequest request, StaplerResponse response) throws IOException {
		if(userCanAdd()) {
			String content = request.getParameter("content");
			String title = request.getParameter("title");
			String oldFilename = request.getParameter("oldFilename");
			File file;
			if(FileUtils.readFileToString(new File(oldFilename), CodeSnippetAction.UTF_8).equals(content)) {
				file = addFile(content, title, oldFilename);
			} else {
				file = addFile(content, title, null);
			}
			response.sendRedirect("?file="+file.getName()+"&title="+title);
		} else {
			response.setStatus(403);
		}
	}

	public void doGet(StaplerRequest request, StaplerResponse response) throws IOException {
		if(userCanRead()) {
			String filename = request.getParameter("filename");
			String title = request.getParameter("title");
			File file = new File(DIRECTORY, filename);
			Snippet snippet = findSnippet(file, title);
			response.setContentType("text/plain");
			PrintWriter writer = response.getWriter();
			writer.print(snippet.readContents());
			writer.flush();
		} else {
			response.setStatus(403);
		}
	}

	public void doDeleteAll(StaplerRequest request, StaplerResponse response) throws IOException {
		if(userCanDelete()) {
			FileUtils.cleanDirectory(DIRECTORY);
		} else {
			response.setStatus(403);
		}
		response.sendRedirect("?");
	}

	public void doDelete(StaplerRequest request, StaplerResponse response) throws IOException {
		if(userCanDelete()) {
			String filename = request.getParameter("filename");
			File file = new File(DIRECTORY, filename);
			if(file.exists()) {
				if(file.delete()) {
					response.setStatus(500);
					LOG.warning("Couldn't delete file " + file.getAbsolutePath());
				}
			} else {
				response.setStatus(404);
			}
		} else {
			response.setStatus(403);
		}
		response.sendRedirect("?");
	}

	public boolean userCanRead() {
		return userHasPermission(AbstractProject.READ);
	}

	public boolean userCanAdd() {
		return userHasPermission(AbstractProject.CREATE);
	}

	public boolean userCanDelete() {
		return userHasPermission(AbstractProject.DELETE);
	}

	private boolean userHasPermission(Permission permission) {
		ACL acl = Jenkins.getInstance().getACL();
		return acl.hasPermission(Jenkins.getAuthentication(), permission);
	}

	public Snippet snippetFromRequest(StaplerRequest request) throws IOException {
		String filePath = request.getParameter("file");

		String title = request.getParameter("title");
		if(filePath == null) {
			return null;
		}

		File file = new File(DIRECTORY, filePath);
		Snippet snippet = new Snippet(file);
		snippet.setTitle(title);
		return snippet;
	}

	public File addFile(String contents, String title, String filename) throws IOException {
		verifyRootDirectoryExists();
		String currentUserId = findCurrentUserId();
		long currentTime = System.currentTimeMillis();

		if(filename == null) {
			File file = new File(DIRECTORY, currentUserId + "-" + currentTime + ".txt");
			File titleFile = new File(DIRECTORY, currentUserId + "-" + currentTime + ".title");
			int unique = 0;
			while(file.exists()) {
				file = new File(DIRECTORY, currentUserId + "-" + currentTime + "." + ++unique + ".txt");
				titleFile = new File(DIRECTORY, currentUserId + "-" + currentTime + "." + ++unique + ".title");
			}

			FileUtils.writeStringToFile(file, contents, UTF_8);
			FileUtils.writeStringToFile(titleFile, title, UTF_8);
			return file;
		} else {
			File oldFile = new File(filename);
			if (!oldFile.exists()) {
				throw new RuntimeException("This should not happen!");
			}
			File titleFile = new File(filename.substring(0, filename.length() - ".txt".length()) + ".title");
			FileUtils.writeStringToFile(titleFile, title, UTF_8);
			return oldFile;
		}
	}

	public Snippet findSnippet(File fileName, String title) throws IOException {
		verifyRootDirectoryExists();
		Snippet snippet = new Snippet(fileName);
		snippet.setTitle(title);
		return snippet;
	}

	public List<Snippet> listSnippets() throws IOException {
		verifyRootDirectoryExists();
		File[] files = DIRECTORY.listFiles(new FileFilter() {
			public boolean accept(File file) {
				return file.isFile() && !file.isHidden();
			}
		});

		List<Snippet> result = Collections.emptyList();
		if(files != null) {
			result = new ArrayList<Snippet>(files.length);
			for (File file : files) {
				if(!file.getName().endsWith(".txt")) continue;
				Snippet snippet = new Snippet(file);
				snippet.setTitle("untitled");
				for (File title : files) {
					if(!title.getName().endsWith(".title")) continue;
					if(file.getName().substring(0, file.getName().length() - ".txt".length()).equals(title.getName().substring(0, title.getName().length() - ".title".length()))) {
						snippet.setTitle(FileUtils.readFileToString(title, CodeSnippetAction.UTF_8));
					}

				}

				result.add(0, snippet);

			}
		}
		Collections.sort(result);
		return result;
	}

	private String findCurrentUserId() {
		String userId = "anonymous";
		User current = User.current();
		if(current != null) {
			userId = current.getId();
		}
		return userId;
	}

	private void verifyRootDirectoryExists() throws IOException {
		if(!DIRECTORY.exists() && !DIRECTORY.mkdirs()) {
			throw new IOException(DIRECTORY.getAbsolutePath() + " cannot be created.");
		}
	}

	public String getIconFileName() {
		return "/plugin/code-snippets/code.png";
	}

	public String getDisplayName() {
		return Messages.CodeSnippetDisplayName();
	}

	public String getUrlName() {
		return "code-snippets";
	}
}
