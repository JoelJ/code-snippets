package com.joelj.jenkins.codesnippets;

import hudson.model.User;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * User: Joel Johnson
 * Date: 2/23/13
 * Time: 12:44 PM
 */
public class Snippet {
	private final File file;
	private final String userId;
	private Date date;

	public Snippet(File file) {
		this.file = file;
		String fileName = file.getName();
		String[] split = fileName.split("-", 2);
		String userId = split[0];
		String dateStr = split[1].split("\\.",2)[0];

		this.userId = userId;
		this.date = new Date(Long.parseLong(dateStr));
	}

	public File getFile() {
		return file;
	}

	public String getUserId() {
		return userId;
	}

	public Date getDate() {
		return date;
	}

	public String createFormattedDate() {
		return date.toString();
	}

	public String readContents() throws IOException {
		return FileUtils.readFileToString(getFile(), CodeSnippetAction.UTF_8);
	}

	public User findUser() {
		if(getUserId().equals("anonymous")) {
			return User.getUnknown();
		}
		return User.get(getUserId());
	}
}
