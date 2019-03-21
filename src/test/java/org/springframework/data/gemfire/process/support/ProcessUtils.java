/*
 * Copyright 2010-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.data.gemfire.process.support;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Scanner;
import java.util.logging.Logger;

import org.springframework.data.gemfire.test.support.FileSystemUtils;
import org.springframework.data.gemfire.test.support.IOUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * The {@link ProcessUtils} class is a utility class for working with Operating System (OS) {@link Process processes}.
 *
 * @author John Blum
 * @see java.io.File
 * @see java.lang.Process
 * @since 1.5.0
 */
@SuppressWarnings("unused")
public abstract class ProcessUtils {

	protected static final Logger log = Logger.getLogger(ProcessUtils.class.getName());

	protected static final String TERM_TOKEN = "<TERM/>";

	/* (non-Javadoc) */
	public static int currentPid() {
		RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
		String runtimeMXBeanName = runtimeMXBean.getName();

		Exception cause = null;

		if (StringUtils.hasText(runtimeMXBeanName)) {
			int atSignIndex = runtimeMXBeanName.indexOf('@');

			if (atSignIndex > 0) {
				try {
					return Integer.parseInt(runtimeMXBeanName.substring(0, atSignIndex));
				}
				catch (NumberFormatException e) {
					cause = e;
				}
			}
		}

		throw new PidUnavailableException(String.format("Process ID (PID) not available [%s]", runtimeMXBeanName),
			cause);
	}

	/* (non-Javadoc) */
	public static boolean isAlive(Process process) {
		return (process != null && process.isAlive());
	}

	/* (non-Javadoc) */
	public static boolean isRunning(int processId) {
		/*
		for (VirtualMachineDescriptor vmDescriptor : VirtualMachine.list()) {
			if (String.valueOf(processId).equals(vmDescriptor.id())) {
				return true;
			}
		}

		return false;
		*/

		throw new UnsupportedOperationException("operation not supported");
	}

	/* (non-Javadoc) */
	public static boolean isRunning(Process process) {
		try {
			process.exitValue();
			return false;
		}
		catch (IllegalThreadStateException ignore) {
			return true;
		}
	}

	/* (non-Javadoc) */
	public static void signalStop(Process process) throws IOException {
		if (isRunning(process)) {
			OutputStream processOutputStream = process.getOutputStream();
			processOutputStream.write(TERM_TOKEN.concat("\n").getBytes());
			processOutputStream.flush();
		}
	}

	/* (non-Javadoc) */
	@SuppressWarnings("all")
	public static void waitForStopSignal() {
		Scanner in = new Scanner(System.in);
		while (!TERM_TOKEN.equals(in.next()));
	}

	/* (non-Javadoc) */
	public static int findAndReadPid(File workingDirectory) {
		File pidFile = findPidFile(workingDirectory);

		if (pidFile == null) {
			throw new PidUnavailableException(String.format(
				"No PID file was found in working directory [%s] or any of it's sub-directories",
					workingDirectory));
		}

		return readPid(pidFile);
	}

	/* (non-Javadoc) */
	@SuppressWarnings("all")
	protected static File findPidFile(File workingDirectory) {
		Assert.isTrue(FileSystemUtils.isDirectory(workingDirectory), String.format(
			"File [%s] is not a valid directory", workingDirectory));

		for (File file : workingDirectory.listFiles(DirectoryPidFileFilter.INSTANCE)) {
			if (file.isDirectory()) {
				file = findPidFile(file);
			}

			if (PidFileFilter.INSTANCE.accept(file)) {
				return file;
			}
		}

		return null;
	}

	/* (non-Javadoc) */
	@SuppressWarnings("all")
	public static int readPid(File pidFile) {
		Assert.isTrue(pidFile != null && pidFile.isFile(), String.format(
			"File [%s] is not a valid file", pidFile));

		BufferedReader fileReader = null;
		String pidValue = null;

		try {
			fileReader = new BufferedReader(new FileReader(pidFile));
			pidValue = String.valueOf(fileReader.readLine()).trim();

			return Integer.parseInt(pidValue);
		}
		catch (FileNotFoundException e) {
			throw new PidUnavailableException(String.format("PID file [%s] not found", pidFile), e);
		}
		catch (IOException e) {
			throw new PidUnavailableException(String.format("failed to read PID from file [%s]", pidFile), e);
		}
		catch (NumberFormatException e) {
			throw new PidUnavailableException(String.format(
				"value [%1$s] from PID file [%2$s] was not a valid numerical PID", pidValue, pidFile), e);
		}
		finally {
			IOUtils.close(fileReader);
		}
	}

	/* (non-Javadoc) */
	@SuppressWarnings("all")
	public static void writePid(File pidFile, int pid) throws IOException {
		Assert.isTrue(pidFile != null && (pidFile.isFile() || pidFile.createNewFile()), String.format(
			"File [%s] is not a valid file", pidFile));

		Assert.isTrue(pid > 0, String.format("PID [%d] must greater than 0", pid));

		PrintWriter fileWriter = new PrintWriter(new BufferedWriter(new FileWriter(pidFile, false), 16), true);

		try {
			fileWriter.println(pid);
		}
		finally {
			pidFile.deleteOnExit();
			FileSystemUtils.close(fileWriter);
		}
	}

	/* (non-Javadoc) */
	protected static class DirectoryPidFileFilter extends PidFileFilter {

		protected static final DirectoryPidFileFilter INSTANCE = new DirectoryPidFileFilter();

		/**
		 * @inheritDoc
		 */
		@Override
		public boolean accept(File path) {
			return (path != null && (path.isDirectory() || super.accept(path)));
		}
	}

	/* (non-Javadoc) */
	protected static class PidFileFilter implements FileFilter {

		protected static final PidFileFilter INSTANCE = new PidFileFilter();

		/**
		 * @inheritDoc
		 */
		@Override
		public boolean accept(File path) {
			return (path != null && path.isFile() && path.getName().toLowerCase().endsWith(".pid"));
		}
	}
}
